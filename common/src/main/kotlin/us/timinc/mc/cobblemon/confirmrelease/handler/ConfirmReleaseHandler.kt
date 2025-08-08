package us.timinc.mc.cobblemon.confirmrelease.handler

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Holders.CONFIRM_RELEASE
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Network.sendClientPacket
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.TranslationComponents.releaseConfirmed
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.config
import us.timinc.mc.cobblemon.confirmrelease.network.ConfirmReleaseReceipt
import us.timinc.mc.cobblemon.timcore.AbstractHandler

object ConfirmReleaseHandler : AbstractHandler<ReleasePokemonEvent.Pre>() {
    override fun handle(evt: ReleasePokemonEvent.Pre) {
        val pokemon = evt.pokemon
        if (!config.confirmFor.any { it.matches(pokemon) }) return

        evt.cancel()

        val player = evt.player
        val receipt = ConfirmReleaseReceipt.Data(pokemon)
        val packetId = CONFIRM_RELEASE.hangReceipt(player, receipt)
        val packet = receipt.toPacket(packetId)
        sendClientPacket(packet, player)
    }

    fun finishRelease(player: ServerPlayer, pokemon: Pokemon) {
        val storage = pokemon.storeCoordinates.get()!!.store
        storage.remove(pokemon)

        player.sendSystemMessage(releaseConfirmed(pokemon))

        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.post(ReleasePokemonEvent.Post(player, pokemon, storage))
    }
}