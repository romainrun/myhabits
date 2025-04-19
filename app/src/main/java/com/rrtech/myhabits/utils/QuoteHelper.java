package com.rrtech.myhabits.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuoteHelper {

    private static final List<String> quotes = Arrays.asList(
            "Commence chaque journée avec une intention claire.",
            "Ta constance vaut plus que ta motivation.",
            "Une habitude à la fois, un jour à la fois.",
            "Les petits progrès mènent à de grandes réussites.",
            "Fais-le pour la personne que tu veux devenir.",
            "Chaque jour est une nouvelle chance de progresser.",
            "Même lentement, tu avances toujours.",
            "La discipline te conduit là où la motivation ne suffit pas.",
            "Construis des habitudes, pas des excuses.",
            "Le succès est la somme de petits efforts répétés.",
            "Répète. Même quand tu ne veux pas.",
            "Les actions aujourd’hui forgent ton avenir.",
            "Rappelle-toi pourquoi tu as commencé.",
            "Ta future version te dira merci.",
            "N’attends pas la motivation. Agis.",
            "Les jours difficiles construisent ta force.",
            "Une seule bonne action peut changer ta journée.",
            "La régularité est la clé du changement durable.",
            "Un jour tu regarderas en arrière avec fierté.",
            "Sois patient. Chaque pas compte.",
            "Tu es plus proche que tu ne le crois.",
            "Chaque habitude te façonne.",
            "Fais-le pour toi. Pas pour les autres.",
            "Un bon jour commence par une bonne habitude.",
            "Une victoire par jour, c’est déjà une victoire.",
            "Arrête de te juger, continue d’agir.",
            "Ce que tu répètes devient ce que tu es.",
            "Ta routine = ton super pouvoir.",
            "Ne casse pas la chaîne 🔗",
            "Sois fier de toi, aujourd’hui aussi."
    );

    public static String getRandomQuote() {
        Random rand = new Random();
        return quotes.get(rand.nextInt(quotes.size()));
    }
}
