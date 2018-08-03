package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.TypedEntity
import com.google.appengine.api.datastore.Entity
import org.junit.Rule
import org.junit.Test
import rule.DatastoreRule
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class TypedEntityTest {
    
    @Rule
    @JvmField
    val context: DatastoreRule = DatastoreRule()
    
    @Test
    fun testTypedEntity(){
        
        val initialEntity = Entity("Session")
        val typedEntity = TypedEntity(initialEntity)

        val now = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)
        
        typedEntity.setIndexedProperty("sessionId", "SID123")
        typedEntity.setUnindexedDateTimeValue("expiresOn", now)

        
        println(typedEntity.string("sessionId"))
        println(typedEntity.dateTimeValueOrNull("expiresOn"))
        println(typedEntity.raw())
        
    }
    
}