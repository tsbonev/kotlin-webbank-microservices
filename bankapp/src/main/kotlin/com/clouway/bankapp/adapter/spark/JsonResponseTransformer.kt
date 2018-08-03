package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.JsonSerializer
import spark.ResponseTransformer

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class JsonResponseTransformer(private val transformer: JsonSerializer) : ResponseTransformer {

    override fun render(model: Any): String {
        return transformer.toJson(model)
    }

}