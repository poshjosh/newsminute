package com.looseboxes.idisc.common.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bc.currencyrateservice.Currencyrate;
import com.bc.currencyrateservice.CurrencyrateService;
import com.bc.currencyrateservice.YahooCurrencyrateService;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.asynctasks.DownloadToLocalCache;
import com.looseboxes.idisc.common.io.IOWrapper;
import com.looseboxes.idisc.common.io.JsonListIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.looseboxes.idisc.common.util.CachedMap;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.StaticResourceManager;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InfoProvider implements Comparator<JSONObject> {

    public static final Locale NGN_ENG = new Locale("en","NG","");

    public enum Category{exchangeRates("Exchange Rates"), countriesOfTheWorld("Info on Countries of the World"),
        citiesOfTheWorld("Info on Cities of the World"), languagesOfTheWorld("Info on languages of the World"),
        governmentwebsitesOfTheWorld("Government Websites of the World");

        String mLabel;
        Category() {
            this.mLabel = this.name();
        }
        Category(String label) {
            this.mLabel = label;
        }
        public String toString() {
            return mLabel;
        }
        public static Category getCategory(String sval) {
            Category category = null;
            try{
                category = valueOfLabel(sval);
                if(category == null) {
                    category = valueOf(sval);
                }
            }catch(Exception e) { }
            return category;
        }
        public static Category valueOfLabel(String label) {
            Category [] categories = values();
            for(Category category:categories) {
                if(category.mLabel.equals(label)) {
                    return category;
                }
            }
            return null;
        }
    }

    public List<JSONObject> getInfo(Context context, Category category) {

        Logx.debug(this.getClass(), "Selected category: {0}", category);

        List<JSONObject> output;
        switch(category) {
            case countriesOfTheWorld:
                output = this.getCountriesOfTheWorld(context); break;
            case exchangeRates:
                output = this.getCurrencyRates(context); break;
            default:
                throw new IllegalArgumentException("Unexpected "+Category.class.getName()+". Expected any of: "+(Arrays.toString(Category.values()))+". Found: "+category);
        }
        return output;
    }

    private List<JSONObject> _cr;
    public List<JSONObject> getCurrencyRates(Context context) {
        if(_cr == null) {
            try{

                Set<Currency> currencies = this.getCurrencies(context);

                _cr = new ArrayList<>(currencies.size());

                for(Currency currency:currencies) {

                    StringBuilder data = this.getCurrencyRatesData(context, currency, currencies);

                    JSONObject exchangeRateFeed = JsonView.getDefaultJsonObject(
                            context, "Exchange rate converting from " + this.getDisplayName(context, currency)+" to other currencies", data, Category.exchangeRates);

                    _cr.add(exchangeRateFeed);
                }

                Collections.sort(_cr, this);

            }catch(Exception e) {
                Logx.log(this.getClass(), e);
            }
        }

        return _cr;
    }

    @Override
    public int compare(JSONObject a, JSONObject b) {
        Feed feed = this.getFeed();
        feed.setJsonData(a);
        String s1 = feed.getHeading("");
        feed.setJsonData(b);
        String s2 = feed.getHeading("");
        return s1.compareTo(s2);
    }

    private class CurrencyNameComparator implements Comparator<Currency> {
        private Context context;
        private CurrencyNameComparator(Context context){
            this.context = context;
        }
        @Override
        public int compare(Currency a, Currency b) {
            String s1 = InfoProvider.this.getDisplayName(context, a);
            if(s1 == null) {
                s1 = "";
            }
            String s2 = InfoProvider.this.getDisplayName(context, b);
            if(s2 == null) {
                s2 = "";
            }
            return s1.compareTo(s2);
        }
    }

    private Feed _f;
    private Feed getFeed() {
        if(_f == null) {
            _f = new Feed();
        }
        return _f;
    }

    public StringBuilder getCurrencyRatesData(Context context, Currency from, Set<Currency> to) {
        if(to != null && !to.isEmpty()) {
            StringBuilder content = new StringBuilder(to.size() * 40);
            String titlePart = "Exchange Rates Converting From: ";
            content.append("<html><head><title>").append(titlePart).append(from.getCurrencyCode());
            String displayName = this.getDisplayName(context, from);
            content.append("</title></head><body><h3>One (1) ").append(displayName);
            boolean hasDisplayName = !displayName.equals(from.getSymbol());
            if(hasDisplayName) {
                content.append(" (").append(from.getSymbol()).append(")");
            }
            content.append(" is equivalent to:</h3>");
            content.append("<table>");
            content.append("<tr><th>Code</th><th>Rate</th>");
            if(hasDisplayName) {
                content.append("<th>Name</th>");
            }
            content.append("</tr>");
            for (Currency toCode : to) {
                if (toCode == null) {
                    continue;
                }
                float rate = this.getExchangeRate(context, from.getCurrencyCode(), toCode.getCurrencyCode());
                content.append("<tr><td>").append(toCode).append("</td>");
                if(rate != -1.0f) {
                    content.append("<td>").append(rate).append("</td>");
                }else{
                    content.append("<td>Not available</td>");
                }
                if(hasDisplayName) {
                    content.append("<td>").append(this.getDisplayName(context, toCode)).append("</td>");
                }
                content.append("</tr>");
            }
            content.append("</table></body></html>");
            return content;
        }else {
            return null;
        }
    }

    @TargetApi(19)
    private String getDisplayName(Context context, Currency currency) {
        String output;
        // We use a literal int as we don't know if support for value Build.VERSION is available
        if(App.isAcceptableVersion(context, 19)) {
            output = currency.getDisplayName();
        }else{
            output = currency.toString();
        }
        return output;
    }

    private float getExchangeRate(Context context, String fromCode, String toCode) {
        if(fromCode == null || toCode == null) {
            return -1.0f;
        }else if(fromCode.equals(toCode)) {
            return 1.0f;
        }else{
            final CurrencyrateService currencyrateService = this.getCurrencyrateService(context);
            Currencyrate currencyrate = currencyrateService.getRate(fromCode, toCode);
            return currencyrate == null ? -1.0f : currencyrate.getRate();
        }
    }

    private Currency getCurrency(Locale locale) {
        try {
            return Currency.getInstance(locale);
        }catch(Exception e) {
            // Currency.getInstance(Locale) may throw java.lang.IllegalArgumentException
//            Logx.log(this.getClass(), e);
            return null;
        }
    }

    public List<JSONObject> getCountriesOfTheWorld(Context context) {

        return this.getOpenGeoCodeData(context, Category.countriesOfTheWorld, TimeUnit.DAYS.toMillis(7));
    }

    public List<JSONObject> getCitiesOfTheWorld(Context context) {

        return this.getOpenGeoCodeData(context, Category.citiesOfTheWorld, TimeUnit.DAYS.toMillis(7));
    }

    public List<JSONObject> getLanguagesOfTheWorld(Context context) {

        return this.getOpenGeoCodeData(context, Category.languagesOfTheWorld, TimeUnit.DAYS.toMillis(7));
    }

    public List<JSONObject> getGovernmentWebsitesOfTheWorld(Context context) {

        return this.getOpenGeoCodeData(context, Category.governmentwebsitesOfTheWorld, TimeUnit.DAYS.toMillis(7));
    }

    /**
     * @return Data pulled from http://opengeocode.org/download.php
     */
    public List<JSONObject> getOpenGeoCodeData(
            final Context context, final Category key, final long downloadInterval) {

        if(context == null || key == null) {
            throw new NullPointerException();
        }

        final String filename = InfoProvider.class.getName()+"."+key.name()+".data.json";

        final JsonListIO<JSONObject> io = new JsonListIO<>(context, filename);

        List<JSONObject> target = io.getTarget();

        if(target != null) {

            return target;

        }else{

            StaticResourceManager<List<JSONObject>> srm = new StaticResourceManager<List<JSONObject>>(
                    context, downloadInterval, InfoProvider.class.getName()+"."+key+".lastDownloadTime.long") {

                @Override
                protected IOWrapper<List<JSONObject>> createIOWrapper(Context context) {
                    return io;
                }
                @Override
                protected void update(IOWrapper<List<JSONObject>> ioWrapper) {
                    DownloadToLocalCache<List<JSONObject>> d = new DownloadToLocalCache<List<JSONObject>>(context, key.name(), io){
                        @Override
                        public String getTarget() {
                            return InfoProvider.this.getUrl(context,key);
                        }
                        @Override
                        public ResponseParser getResponseParser() {
                            return new CountriesOfTheWorldParser(context);
                        }
                    };
                    d.setNoUI(true);
                    d.execute();
                }
            };

            srm.update(false);

            return null;
        }
    }

    public Category getCategoryForLabel(String label) {
        Category [] categories = Category.values();
        for(Category category:categories) {
            if(category.toString().equals(label)) {
                return category;
            }
        }
        return null;
    }

    private String getUrl(Context context, Category key) {
        String url;
        switch(key) {
            case countriesOfTheWorld:
                url = App.getPropertiesManager(context).getString(PropertiesManager.PropertyName.countriesOfTheWorldDataEndpoint);
                break;
            case citiesOfTheWorld:
                url = App.getPropertiesManager(context).getString(PropertiesManager.PropertyName.citiesOfTheWorldDataEndpoint);
                break;
            case languagesOfTheWorld:
                url = App.getPropertiesManager(context).getString(PropertiesManager.PropertyName.languagesOfTheWorldDataEndpoint);
                break;
            case governmentwebsitesOfTheWorld:
                url = App.getPropertiesManager(context).getString(PropertiesManager.PropertyName.governmentwebsitesOfTheWorldDataEndpoint);
                break;
            default:
                throw new IllegalArgumentException("Unexpected "+Category.class.getName()+". Expected any of: "+(Arrays.toString(Category.values()))+". Found: "+key);
        }
        return url;
    }

    public Set<String> getCurrencyCodes(Context context) {
        Set<Currency> currencies = this.getCurrencies(context);
        Set<String> codes = new LinkedHashSet<>();
        for(Currency currency:currencies) {
            String code = currency.getCurrencyCode();
            if(code != null) {
                codes.add(code);
            }
        }
        return codes;
    }

    private Locale [] _locs;
    public Locale [] getLocales() {
        if(_locs == null) {
            Locale [] locs = Locale.getAvailableLocales();
            LinkedList<Locale> list = new LinkedList<>(Arrays.asList(locs));
            if(!list.contains(NGN_ENG)) {
                list.add(0, NGN_ENG);
            }
            _locs = list.toArray(new Locale[list.size()]);
        }
        return _locs;
    }

    private Set<Currency> _curr;
    public Set<Currency> getCurrencies(Context context) {
        if(_curr == null) {
            try{

                Locale [] locales = this.getLocales();

                Logx.debug(this.getClass(), "Available Locales: {0}", locales==null?null:locales.length);

                _curr = new HashSet<>(locales.length);

                for(Locale locale:locales) {

                    Currency currency = this.getCurrency(locale);
                    if(currency == null) {
                        continue;
                    }
                    if(currency.getCurrencyCode() == null) {
                        continue;
                    }
                    _curr.add(currency);
                }

                ArrayList<Currency> list = new ArrayList<>(_curr);

                Collections.sort(list, new CurrencyNameComparator(context));

                _curr = Collections.unmodifiableSet(new LinkedHashSet<>(list));

            }catch(Exception e) {
                Logx.log(this.getClass(), e);
            }
        }

        return _curr;
    }

    private CurrencyrateService _crs;
    public CurrencyrateService getCurrencyrateService(Context context) {
        if(_crs == null) {
            _crs = new CurrencyrateServiceImpl(context);
        }
        return _crs;
    }

    private class CurrencyrateServiceImpl extends YahooCurrencyrateService {

        private Context context;

        private CurrencyrateServiceImpl(Context context) {
            this.context = context;
        }

        @Override
        public Currencyrate getRate(final String fromCode, final String toCode) {

            Currencyrate rate = this.getCachedRate(fromCode, toCode);

            if(rate != null && !this.isExpired(rate)) {

                return rate;

            }else {

                AsyncTask<String, Void, Currencyrate[]> downloadRates = new AsyncTask<String, Void, Currencyrate[]>() {
                    @Override
                    protected Currencyrate [] doInBackground(String... params) {

                        Set<String> codes = InfoProvider.this.getCurrencyCodes(context);

                        List<String> toCodes_list = new LinkedList<>();
                        for(String code:codes) {
                            if(!code.equals(fromCode)) {
                                toCodes_list.add(code);
                            }
                        }
                        String [] toCodes = toCodes_list.toArray(new String[toCodes_list.size()]);

                        String [] fromCodes = new String[toCodes.length];
                        Arrays.fill(fromCodes, fromCode);

                        Currencyrate [] output;
                        try {
                            output = CurrencyrateServiceImpl.this.getRates(fromCodes, toCodes);
                        }catch(Exception e) {
//                            Logx.log(this.getClass(), e);
                            // Lighter logging for this
                            Logx.log(Log.WARN, this.getClass(), e.toString());
                            output = null;
                        }finally{
                            CurrencyrateServiceImpl.this.getCache().close();
                        }
                        return output;
                    }
                };

                downloadRates.execute(fromCode);

                return null;
            }
        }

        private CachedMap<String, Currencyrate> _currencyrateCache_accessViaGetter;
        @Override
        protected CachedMap<String, Currencyrate> getCache() {
            if(_currencyrateCache_accessViaGetter == null) {
                _currencyrateCache_accessViaGetter = new CachedMap<>(context, YahooCurrencyrateService.class.getName()+".cachedRates.map");
            }
            return _currencyrateCache_accessViaGetter;
        }
    }
}
