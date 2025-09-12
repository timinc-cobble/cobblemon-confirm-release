package us.timinc.mc.cobblemon.confirmrelease.data

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import us.timinc.mc.cobblemon.timcore.AbstractReloadListener
import us.timinc.mc.cobblemon.timcore.PokemonMatcher

class ReleaseGuard(
    val matcher: PokemonMatcher,
    val priority: Int,
    val message: String,
) {
    var id: ResourceLocation? = null

    companion object {
        val CODEC: Codec<ReleaseGuard> = RecordCodecBuilder.create { instance ->
            instance.group(
                PokemonMatcher.CODEC.fieldOf("matcher").forGetter { it.matcher },
                Codec.INT.fieldOf("priority").forGetter { it.priority },
                Codec.STRING.fieldOf("message").forGetter { it.message }
            ).apply(instance, ::ReleaseGuard)
        }
    }

    object Manager : AbstractReloadListener(Gson(), "release_guard") {
        private var guards: MutableMap<Int, MutableList<ReleaseGuard>> = mutableMapOf()

        override fun apply(
            objectMap: MutableMap<ResourceLocation, JsonElement>,
            resourceManager: ResourceManager,
            profilerFiller: ProfilerFiller,
        ) {
            guards.clear()
            objectMap.entries.forEach { (id, json) ->
                val guard = CODEC.parse(JsonOps.INSTANCE, json).orThrow
                guard.id = id
                if (!guards.containsKey(guard.priority)) guards[guard.priority] = mutableListOf()
                val priorityList = guards[guard.priority]!!
                priorityList.add(guard)
            }
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