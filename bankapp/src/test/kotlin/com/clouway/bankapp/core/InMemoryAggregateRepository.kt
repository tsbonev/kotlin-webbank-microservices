package com.clouway.bankapp.core

import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.Repository
import com.clouway.kcqrs.core.AggregateRoot
import com.clouway.kcqrs.core.Event
import java.util.*

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class InMemoryAggregateRepository : Repository {
    private val aggregateIdToEvents = mutableMapOf<UUID, MutableList<Event>>()

    override fun <T : AggregateRoot> save(aggregate: T) {
        val changes = aggregate.getUncommittedChanges().toMutableList()

        if (aggregateIdToEvents.containsKey(aggregate.getId())) {
            aggregateIdToEvents[aggregate.getId()]!!.addAll(changes)
        } else {
            aggregateIdToEvents[aggregate.getId()!!] = changes
        }

        aggregate.markChangesAsCommitted()
    }

    override fun <T : AggregateRoot> getById(id: UUID, type: Class<T>): T {
        if (!aggregateIdToEvents.containsKey(id)) {
            throw AggregateNotFoundException(id)
        }
        val events = aggregateIdToEvents[id]

        val instance = type.newInstance() as T
        instance.loadFromHistory(events!!)
        return instance
    }
}