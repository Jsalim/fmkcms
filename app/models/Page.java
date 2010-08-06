/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 *
 * @author waxzce
 */
@Entity
@Indexed(index = "fmkpage")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all")
public class Page extends Model {

    @Required
    @Field
    public String title;

    @Required
    @Lob
    @Field
    @MaxSize(60000)
    public String content;

    @Required
    public String urlId;

    @IndexedEmbedded
    @ManyToMany(cascade = CascadeType.PERSIST)
    public Set<Tag> tags;

    @Required
    public Locale lang;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    public Map<String, Page> otherLanguages;

    @Required
    public Boolean published;

    public static Page getByUrlId(String urlId) {
        Page p = Page.find("urlId = ?", urlId).first();
        return p;
    }

    public Page tagItWith(String name) {
        tags.add(Tag.findOrCreateByName(name));
        return this;
    }

    public void publish() {
        this.published = true;
        this.save();
    }

    public void unPublish() {
        this.published = false;
        this.save();
    }

    public void addTranslation(Page translated) {
        if (this.lang.getLanguage().equals(translated.lang.getLanguage())) {
            return;
        }
        this.otherLanguages.put(translated.lang.getLanguage(), translated);
        translated.otherLanguages.put(this.lang.getLanguage(), this);
        this.save();
        translated.save();
    }

    public Page getTranslation(Locale lang) {
        if (this.lang.getLanguage().equals(lang.getLanguage())) {
            return this;
        }
        return this.otherLanguages.get(lang.getLanguage());
    }

    public static List<Page> findTaggedWith(String tag) {
        return Page.find(
                "select distinct p from Page p join p.tags as t where t.name = ?", tag).fetch();
    }

    @PrePersist
    public void tagsManagement() {
        if (tags != null) {
            Set<Tag> newTags = new TreeSet<Tag>();
            Iterator<Tag> it = tags.iterator();
            while (it.hasNext()) {
                Tag tag = it.next();
                newTags.add(Tag.findOrCreateByName(tag.name));
            }
            this.tags = newTags;
        }
    }
}
