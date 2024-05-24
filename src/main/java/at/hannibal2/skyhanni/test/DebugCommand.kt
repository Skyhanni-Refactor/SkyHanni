package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.repo.RepoManager
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.system.OS

object DebugCommand {

    fun command(args: Array<String>) {
        val list = mutableListOf<String>()
        list.add("```")
        list.add("= Debug Information for SkyHanni ${SkyHanniMod.version} =")
        list.add("")

        val search = args.joinToString(" ")
        list.add(
            if (search.isNotEmpty()) {
                if (search.equalsIgnoreColor("all")) {
                    "search for everything:"
                } else "search '$search':"
            } else "no search specified, only showing interesting stuff:"
        )

        val event = DebugDataCollectEvent(list, search)

        // calling default debug stuff
        player(event)
        repoAutoUpdate(event)
        repoLocation(event)
        globalRender(event)
        skyblockStatus(event)
        profileName(event)
        profileType(event)

        event.post()

        if (event.empty) {
            list.add("")
            list.add("Nothing interesting to show right now!")
            list.add("Looking for something specific? /shdebug <search>")
            list.add("Wanna see everything? /shdebug all")
        }

        list.add("```")
        OS.copyToClipboard(list.joinToString("\n"))
        ChatUtils.chat("§eCopied SkyHanni debug data in the clipboard.")
    }

    private fun profileType(event: DebugDataCollectEvent) {
        event.title("Profile Type")
        if (!SkyBlockAPI.isConnected) {
            event.addIrrelevant("Not on SkyBlock")
            return
        }

        event.addData("on ${SkyBlockAPI.gamemode.name.lowercase()}")
    }

    private fun profileName(event: DebugDataCollectEvent) {
        event.title("Profile Name")
        if (!SkyBlockAPI.isConnected) {
            event.addIrrelevant("Not on SkyBlock")
            return
        }

        event.addIrrelevant {
            add("profileName: '${SkyBlockAPI.profileName ?: "NULL"}'")
            add("profileId: '${SkyBlockAPI.profileId ?: "NULL"}'")
        }
    }

    private fun skyblockStatus(event: DebugDataCollectEvent) {
        event.title("SkyBlock Status")
        if (!HypixelAPI.onHypixel) {
            event.addData("not on Hypixel")
            return
        }
        if (!SkyBlockAPI.isConnected) {
            event.addData("not on SkyBlock, but on Hypixel")
            return
        }
        event.addIrrelevant {
            add("on Hypixel SkyBlock")
            add("skyBlockIsland: ${SkyBlockAPI.island}")
            add("skyBlockArea: '${SkyBlockAPI.area}'")
        }
    }

    private fun globalRender(event: DebugDataCollectEvent) {
        event.title("Global Render")
        if (SkyHanniDebugsAndTests.globalRender) {
            event.addIrrelevant("normal enabled")
        } else {
            event.addData {
                add("Global renderer is disabled!")
                add("No renderable elements from SkyHanni will show up anywhere!")
            }
        }
    }

    private fun repoAutoUpdate(event: DebugDataCollectEvent) {
        event.title("Repo Auto Update")
        if (SkyHanniMod.feature.dev.repo.repoAutoUpdate) {
            event.addIrrelevant("normal enabled")
        } else {
            event.addData("The repo does not auto update because auto update is disabled!")
        }
    }

    private fun repoLocation(event: DebugDataCollectEvent) {
        event.title("Repo Location")
        event.addIrrelevant("repo location: '${RepoManager.getRepoLocation()}'")
    }

    private fun player(event: DebugDataCollectEvent) {
        event.title("Player")
        event.addIrrelevant {
            add("name: '${McPlayer.name}'")
            add("uuid: '${McPlayer.uuid.toDashlessUUID()}'")
        }
    }
}
