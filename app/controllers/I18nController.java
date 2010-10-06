package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;

/**
 *
 * @author keruspe
 */
public class I18nController extends Controller {

    public static List<Locale> getLanguages() {
        List<Locale> locales = new ArrayList<Locale>();
        Locale tldLocale = I18nController.getTldLanguage();

        locales.addAll(I18nController.getQueryStringLanguages());
        if (tldLocale != null) locales.add(tldLocale);
        locales.addAll(I18nController.getBrowserLanguages());

        return locales;
    }

    public static List<Locale> getQueryStringLanguages() {
        List<Locale> locales = new ArrayList<Locale>();

        String[] queryString = Http.Request.current().querystring.split("&");
        for (int i = 0 ; i < queryString.length ; ++i) {
            String[] current = queryString[i].split("=");
            if (current.length != 2 || !current[0].equalsIgnoreCase("lang"))
                continue;
            String[] locale = current[1].split("-");
            switch (locale.length) {
                case 1:
                    locales.add(new Locale(locale[0]));
                    break;
                default:
                    locales.add(new Locale(locale[0], locale[1].substring(0, 2)));
                    break;
            }
        }

        return locales;
    }

    public static Locale getTldLanguage() {
        String tld4locales = Play.configuration.getProperty("fmkcms.tld4locales", "false");

        if (!tld4locales.equalsIgnoreCase("true"))
            return null;

        Map<String, Locale> tldLocales = new HashMap<String, Locale>();

        // Add your tld specific locales here
        tldLocales.put("com", Locale.ENGLISH);
        tldLocales.put("org", Locale.ENGLISH);

        String[] domainSplitted = Http.Request.current().domain.split("\\.");
        String tld = domainSplitted[domainSplitted.length - 1];

        Locale candidat = tldLocales.get(tld);
        if (candidat != null)
            return candidat;

        /* test whether we're facing an IP or a domain name */
        try {
            Integer.parseInt(tld);
            return null;
        } catch(NumberFormatException e) {
            return new Locale(tld);
        }
    }

    public static List<Locale> getBrowserLanguages() {
        List<Locale> locales = new ArrayList<Locale>();
        List<String> languages = Http.Request.current().acceptLanguage();

        for (String language : languages) {
            if (language.contains("-"))
                locales.add(new Locale(language.split("-")[0], language.split("-")[1]));
            else
                locales.add(new Locale(language));
        }

        return locales;
    }

}
