package com.tzauto.javafxSupport;


import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Control that uses a custom {@link Charset} when reading resource bundles,
 * compared to the default charset which is ISO-8859-1.
 *
 * @author Emil Forslund
 * @since  2.1.6
 */
public final class ResourceBundleControl extends ResourceBundle.Control {

    private final Charset charset;

    public ResourceBundleControl(Charset charset) {
        this.charset = requireNonNull(charset);
    }

    @Override
    public ResourceBundle newBundle(
        final String baseName,
        final Locale locale,
        final String format,
        final ClassLoader loader,
        final boolean reload)
     {
        return ResourceBundle.getBundle("eap", new LanguageUtil().getLocale());//new Locale("zh", "TW");Locale.getDefault()
    }
}