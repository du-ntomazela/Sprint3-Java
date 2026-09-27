package br.com.fiap.specradar.infra;

import java.util.regex.Pattern;

public final class Sanitizador {
    private static final Pattern TAGS_HTML = Pattern.compile("<[^>]*>");
    private static final Pattern CARACTERES_DE_CONTROLE = Pattern.compile("\\p{Cntrl}");

    private Sanitizador() {
    }

    public static String sanitizarTextoLivre(String valor) {
        if (valor == null) {
            return null;
        }
        String semTags = TAGS_HTML.matcher(valor).replaceAll("");
        String semControle = CARACTERES_DE_CONTROLE.matcher(semTags).replaceAll("");
        return semControle.trim().replaceAll("\\s+", " ");
    }
}
