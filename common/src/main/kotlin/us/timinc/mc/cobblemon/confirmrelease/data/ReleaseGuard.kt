package us.timinc.mc.cobblemon.confirmrelease.data

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import us.timinc.mc.cobblemon.timcore.AbstractReloadListener
import us.timinc.mc.cobblemon.timcore.PokemonMatcher
import us.timinc.mc.cobblemon.timcore.getOrNull

class ReleaseGuard(
    val id: ResourceLocation,
    val matcher: PokemonMatcher,
    val priority: Int,
) {
    object Manager : AbstractReloadListener(Gson(), "release_guard") {
        private var guards: MutableMap<Int, MutableList<ReleaseGuard>> = mutableMapOf()

        override fun apply(
            objectMap: MutableMap<ResourceLocation, JsonElement>,
            resourceManager: ResourceManager,
            profilerFiller: ProfilerFiller,
        ) {
            guards.clear()
            objectMap.entries.forEach { (id, json) ->
                val guard = parseGuard(id, json as JsonObject)
                if (!guards.containsKey(guard.priority)) guards[guard.priority] = mutableListOf()
                val priorityList = guards[guard.priority]!!
                priorityList.add(guard)
            }
        }

        private fun parseGuard(id: ResourceLocation, json: JsonObject): ReleaseGuard {
            val priority = json.getOrNull("priority")!!.asInt
            return ReleaseGuard(id, PokemonMatcher(
                json.getOrNull("properties")?.asString ?: "",
                json.getOrNull("labels")?.asJsonArray?.map(JsonElement::getAsString) ?: emptyList(),
                json.getOrNull("anyLabel")?.asBoolean ?: false,
                json.getOrNull("persistentData")?.let {
                    it.asJsonObject.entrySet().fold(mutableMapOf()) { acc, (k, v) ->
                        acc[k] = v.asString
                        acc
                    }
                } ?: mutableMapOf(),
                json.getOrNull("anyPersistentData")?.asBoolean ?: false,
                json.getOrNull("buckets")?.asJsonArray?.map(JsonElement::getAsString) ?: emptyList(),
                json.getOrNull("matchOne")?.asBoolean ?: false
            ), priority)
        }

        fun findMatching(pokemon: Pokemon): ReleaseGuard? {
            val priorities = guards.keys.sorted()
            for (priority in priorities) {
                val priorityList = guards[priority]!!
                val found = priorityList.find { it.matches(pokemon) }
                if (found != null) return found
            }
            return null
        }
    }

    fun matches(pokemon: Pokemon) = matcher.matches(pokemon)
}