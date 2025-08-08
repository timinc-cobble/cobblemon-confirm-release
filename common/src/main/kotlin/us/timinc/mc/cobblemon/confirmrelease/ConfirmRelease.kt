package us.timinc.mc.cobblemon.confirmrelease

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import us.timinc.mc.cobblemon.confirmrelease.data.ReleaseGuard
import us.timinc.mc.cobblemon.confirmrelease.handler.ConfirmReleaseHandler
import us.timinc.mc.cobblemon.confirmrelease.network.ConfirmReleaseReceipt
import us.timinc.mc.cobblemon.timcore.*

const val MOD_ID: String = "confirm_release"

object ConfirmRelease : AbstractMod<ConfirmRelease.ConfirmReleaseConfig>(MOD_ID, ConfirmReleaseConfig::class.java) {

    class ConfirmReleaseConfig : AbstractConfig()

    object Network : AbstractOwoNetwork(modResource("main")) {
        init {
            mainChannel.registerClientbound(
                ConfirmReleaseReceipt.clientPacketClass,
                ConfirmReleaseReceipt::handleClient
            )
            mainChannel.registerServerbound(
                ConfirmReleaseReceipt.serverPacketClass,
                ConfirmReleaseReceipt::handleServer
            )
        }
    }

    object Holders {
        val CONFIRM_RELEASE: Holder<ConfirmReleaseReceipt.Data> = Holder()
    }

    object TranslationComponents {
        fun releaseCancelled(pokemon: Pokemon): MutableComponent =
            Component.translatable("confirm_release.feedback.release_cancelled", pokemon.getDisplayName())

        fun releaseConfirmed(pokemon: Pokemon): MutableComponent =
            Component.translatable("confirm_release.feedback.release_confirmed", pokemon.getDisplayName())

        fun confirmRelease(pokemonName: Component): MutableComponent =
            Component.translatable("confirm_release.ui.confirm_release", pokemonName)
    }

    init {
        Network
        Holders
        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.subscribe(Priority.LOWEST, ConfirmReleaseHandler::handle)
        registerReloadListener(ReleaseGuard.Manager)
    }
}