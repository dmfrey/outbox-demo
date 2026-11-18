/**
 * The shared "published language": the event types that modules exchange.
 *
 * <p>Why a module of its own? {@code order} reacts to inventory's events and {@code inventory}
 * reacts to order's events. If each event lived in its publisher's module that would be a cycle,
 * which {@code ApplicationModules.verify()} rejects. Putting the events here means every module
 * depends only on this one, never on each other.
 */
package com.example.outboxdemo.events;
