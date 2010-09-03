package controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import models.Page;
import models.PageRef;
import models.Tag;
import mongo.MongoEntity;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 *
 * @author waxzce
 * @author keruspe
 */
@With(CheckRights.class)
public class PageController extends Controller {

    public static void page(String urlId) {
        List<Page> pages = Page.getPagesByUrlId(urlId);
        Page page = null;

        switch (pages.size()) {
            case 0:
                notFound();
            case 1:
                page = pages.get(0);
                break;
            default:
                List<Locale> locales = I18nController.getBrowserLanguages();
                for (Locale locale : locales) {
                    // Try exact Locale
                    for (Page candidat : pages) {
                        if ((candidat.language.equals(locale) || (!locale.getCountry().equals("") && candidat.language.getLanguage().equals(locale.getLanguage()))) && candidat.published) {
                            page = candidat;
                            break;
                        }
                    }
                }

                if (page == null)
                    page = pages.get(0); // pick up first for now
        }

        if (! page.published)
            notFound();

        if (request.headers.get("accept").value().contains("json")) {
            renderJSON(page);
        }

        render(page);
    }

    public static void pagesTag(String tagName) {
        render(Page.findTaggedWith(tagName), Tag.findOrCreateByName(tagName));
    }

    public static void newPage(String urlId) {
        render(urlId);
    }

    public static void doNewPage() {
        String urlId = params.get("page.pageReference");
        String title = params.get("page.title");
        String content = params.get("page.content");
        Locale language = params.get("page.language", Locale.class);
        Boolean published = (params.get("page.published") == null) ? Boolean.FALSE : Boolean.TRUE;

        Page page = Page.getFirstPageByUrlId(urlId);
        if (page != null)
            page = page.addTranslation(title, content, language, published);
        else {
            page = new Page();
            page.pageReference = MongoEntity.getDs().find(PageRef.class, "urlId", urlId).get();
            page.title = title;
            page.content = content;
            page.language = language;
            page.published = published;

            validation.valid(page);
            if (Validation.hasErrors()) {

                params.flash(); // add http parameters to the flash scope
                Validation.keep(); // keep the errors for the next request

                PageController.newPage(null);
            } else
                MongoEntity.getDs().save(page);
        }

        if (page.published)
            PageController.page(urlId);
        else
            PageController.page("index");
    }

    public static void newPageRef() {
        render();
    }

    public static void doNewPageRef() {
        String urlId = params.get("pageRef.urlId");
        PageRef pageRef = PageRef.getPageRefByUrlId(urlId);
        Set<Tag> tags = null;

        if (pageRef != null)
            tags = pageRef.tags;
        else {
            pageRef = new PageRef();
            pageRef.urlId = urlId;
        }

        if (tags == null)
            tags = new TreeSet<Tag>();

        String tagsString = params.get("pageRef.tags");
        if (!tagsString.isEmpty()) {
            for (String tag : Arrays.asList(tagsString.split(","))) {
                tags.add(Tag.findOrCreateByName(tag));
            }
        }

        pageRef.tags = tags;

        validation.valid(pageRef);
        if (Validation.hasErrors()) {

            params.flash(); // add http parameters to the flash scope
            Validation.keep(); // keep the errors for the next request

            PageController.newPageRef();
        } else {
            MongoEntity.getDs().save(pageRef);
            PageController.newPage(pageRef.urlId);
        }
    }

    public static void searchPage(String q) {
        // TODO: Reimplement Search
        /*if (q == null) {
            q = "search";
        }

        EntityManager em = JPA.entityManagerFactory.createEntityManager();

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException ex) {
            Logger.getLogger(PageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        org.hibernate.Session s = (org.hibernate.Session) JPA.em().getDelegate();
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(s);
        Transaction tx = fullTextSession.beginTransaction();

        String[] fields = new String[]{"title", "content", "pageReference.urlId", "tags.name"};

        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_20, fields, new StandardAnalyzer(Version.LUCENE_20));
        org.apache.lucene.search.Query query = null;
        try {
            query = parser.parse(q);
        } catch (ParseException ex) {
            Logger.getLogger(PageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, Page.class);
        List<Page> results = hibQuery.list();
        tx.commit();

        render(results, q);*/
        render();
    }
    
    
}
