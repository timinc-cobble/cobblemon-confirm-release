package us.timinc.mc.cobblemon.confirmrelease.network

import com.cobblemon.mod.common.pokemon.Pokemon
import io.wispforest.owo.network.ClientAccess
import io.wispforest.owo.network.ServerAccess
import net.minecraft.network.chat.Component
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Holders.CONFIRM_RELEASE
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.Network.sendServerPacket
import us.timinc.mc.cobblemon.confirmrelease.handler.ConfirmReleaseHandler.finishRelease
import us.timinc.mc.cobblemon.confirmrelease.screen.ConfirmReleaseScreen
import us.timinc.mc.cobblemon.timcore.Holder
import us.timinc.mc.cobblemon.timcore.OwoReceipt
import us.timinc.mc.cobblemon.timcore.TimCore.debugger
import java.util.*

object ConfirmReleaseReceipt :
    OwoReceipt<ConfirmReleaseReceipt.Data, ConfirmReleaseReceipt.Packet, ConfirmReleaseReceipt.Response> {
    @JvmRecord
    data class Packet(
        val id: UUID,
        val name: Component,
    ) {
        fun accept() {
            sendServerPacket(Response(id, true))
        }

        fun reject() {
            sendServerPacket(Response(id, false))
        }
    }

    @JvmRecord
    data class Response(
        val id: UUID,
        val accepted: Boolean,
    )

    class Data(
        val pokemon: Pokemon,
    ) : Holder.ReceiptPacketMaker<Packet> {
        override fun toPacket(id: UUID) = Packet(id, pokemon.getDisplayName())
    }

    override fun handleClient(data: Packet, clientAccess: ClientAccess) {
        clientAccess.runtime().setScreen(ConfirmReleaseScreen(data))
    }

    override fun handleServer(data: Response, serverAccess: ServerAccess) {
        try {
            val receipt = CONFIRM_RELEASE.pullReceipt(data.id, serverAccess.player)
            if (!data.accepted) {
                receipt.player.sendSystemMessage(ConfirmRelease.TranslationComponents.releaseCancelled(receipt.data.pokemon))
                return
            }
            finishRelease(receipt.player, receipt.data.pokemon)
        } catch (e: Error) {
            debugger.debug(e.message ?: "An error occurred while handling ConfirmReleaseReceipt on server.", true)
        }
    }

    override val clientPacketClass: Class<Packet> = Packet::class.java

    override val serverPacketClass: Class<Response> = Response::class.java
}