/**
 * The shared "published language": the event contracts that modules - and, once split out,
 * services - exchange.
 *
 * <p>This is a separate Gradle module with <b>no dependencies</b> (plain Java records), so it can be
 * published to a Maven repository and used by anything, including services that do not use Spring.
 * It holds only the shape of the messages. What each side does with them - and how each side models
 * the same data internally - stays inside the module that owns it.
 *
 * <p>It also keeps the modules acyclic: {@code order} reacts to inventory's events and
 * {@code inventory} reacts to order's, so if each event lived with its publisher the modules would
 * depend on each other. Here every module depends only on this one.
 */
package com.example.outboxdemo.events;
