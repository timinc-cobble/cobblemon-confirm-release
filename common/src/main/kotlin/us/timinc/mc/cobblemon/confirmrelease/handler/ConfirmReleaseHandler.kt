package us.timinc.mc.cobblemon.confirmrelease.handler

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Holders.CONFIRM_RELEASE
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Network.sendClientPacket
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.TranslationComponents.releaseConfirmed
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.debugger
import us.timinc.mc.cobblemon.confirmrelease.data.ReleaseGuard
import us.timinc.mc.cobblemon.confirmrelease.network.ConfirmReleaseReceipt
import us.timinc.mc.cobblemon.timcore.AbstractHandler
import us.timinc.mc.cobblemon.timcore.getIdentifier
import java.util.*

object ConfirmReleaseHandler : AbstractHandler<ReleasePokemonEvent.Pre>() {
    override fun handle(evt: ReleasePokemonEvent.Pre) {
        val caseDebugger = debugger.getCaseDebugger()
        val pokemon = evt.pokemon
        val identifier = pokemon.getIdentifier()
        val releaseGuard = ReleaseGuard.Manager.findMatching(pokemon)
        if (releaseGuard == null) {
            caseDebugger.debug("$identifier does not match any confirm release warnings.")
            return
        }

        caseDebugger.debug("$identifier requires confirmation before release due to release guard ${releaseGuard.id}.")
        evt.cancel()

        val player = evt.player
        val receipt = ConfirmReleaseReceipt.Data(pokemon)
        val packetId = CONFIRM_RELEASE.hangReceipt(player, receipt)
        caseDebugger.debug("Sending confirm release request to client for ${player.name}. ($packetId)")
        val packet = receipt.toPacket(packetId)
        sendClientPacket(packet, player)
    }

    fun finishRelease(player: ServerPlayer, pokemon: Pokemon, id: UUID) {
        val caseDebugger = debugger.getCaseDebugger(id.toString())
        caseDebugger.debug("Finishing release.")

        val storage = pokemon.storeCoordinates.get()!!.store
        storage.remove(pokemon)

        player.sendSystemMessage(releaseConfirmed(pokemon))

        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.post(ReleasePokemonEvent.Post(player, pokemon, storage))
    }
}