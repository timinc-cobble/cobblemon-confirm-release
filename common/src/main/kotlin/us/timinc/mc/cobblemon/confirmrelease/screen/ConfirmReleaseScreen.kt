package us.timinc.mc.cobblemon.confirmrelease.screen

import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.FlowLayout
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.TranslationComponents.confirmRelease
import us.timinc.mc.cobblemon.confirmrelease.ConfirmRelease.modResource
import us.timinc.mc.cobblemon.confirmrelease.network.ConfirmReleaseReceipt

class ConfirmReleaseScreen(private val packet: ConfirmReleaseReceipt.Packet) :
    BaseUIModelScreen<FlowLayout>(FlowLayout::class.java, DataSource.asset(modResource("confirm_release"))) {
    private var responded: Boolean = false

    override fun build(layout: FlowLayout) {
        val confirmLabel = layout.childById(LabelComponent::class.java, "confirm-label")
        confirmLabel.text(confirmRelease(packet.name))

        val acceptButton = layout.childById(ButtonComponent::class.java, "accept-button")
        acceptButton.onPress {
            responded = true
            packet.accept()
            onClose()
        }

        val rejectButton = layout.childById(ButtonComponent::class.java, "reject-button")
        rejectButton.onPress {
            responded = true
            packet.reject()
            onClose()
        }
    }

    override fun dispose() {
        super.dispose()
        if (responded) return
        packet.reject()
    }
}
