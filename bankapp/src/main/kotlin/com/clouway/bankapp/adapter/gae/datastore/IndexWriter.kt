package com.clouway.bankapp.adapter.gae.datastore

import java.util.TreeSet



/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class IndexWriter {

     fun createIndex(vararg words: String?): Set<String> {

        val index = HashSet<String>()
        for (word in words) {

            if (word != null) {
                val lowercaseWord = word.toLowerCase()
                index.add(lowercaseWord)

                var tokens = lowercaseWord.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (token in tokens) {

                    index.addAll(normalizeToken(token))
                }

                tokens = lowercaseWord.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                normalizeTokens(index, tokens)

                tokens = lowercaseWord.split("\\~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                normalizeTokens(index, tokens)

                tokens = lowercaseWord.split("\\:".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                normalizeTokens(index, tokens)

                tokens = lowercaseWord.split("\\-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                normalizeTokens(index, tokens)
            }

        }

        return index
    }

    fun createIndexWithPrefix(prefix: String, vararg words: String): Set<String> {
        val index = createIndex(*words)
        val newSet = HashSet<String>()
        for (string in index) {
            newSet.add(prefix + string)
        }
        return newSet
    }

    private fun normalizeTokens(index: MutableSet<String>, tokens: Array<String>) {
        for (token in tokens) {
            index.addAll(normalizeToken(token))
        }

    }

    private fun normalizeToken(token: String): Set<String> {

        val tokens = TreeSet<String>()

        for (i in 0 until token.length) {
            val word = token.substring(i, token.length)

            tokens.add(word)
        }

        for (i in 1 until token.length) {
            val word = token.substring(0, token.length - i)
            tokens.add(word)
        }

        for (i in 0 until token.length / 2) {
            val word = token.substring(i, token.length - i)

            tokens.add(word)
        }

        return tokens
    }
}