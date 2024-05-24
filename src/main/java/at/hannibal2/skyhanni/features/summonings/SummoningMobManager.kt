package at.hannibal2.skyhanni.features.summonings

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand

object SummoningMobManager {

    private val config get() = SkyHanniMod.feature.combat.summonings

    private val summoningMobs = mutableMapOf<EntityLiving, SummoningMob>()
    private val summoningMobNametags = mutableListOf<EntityArmorStand>()
    private var summoningsSpawned = 0
    private var searchArmorStands = false
    private var searchMobs = false

    private val patternGroup = RepoPattern.group("summoning.mobs")
    private val spawnPattern by patternGroup.pattern( //§aYou have spawned your Tank Zombie §r§asoul! §r§d(249 Mana)
        "spawn",
        "§aYou have spawned your (.+) §r§asoul! §r§d\\((\\d+) Mana\\)"
    )
    private val despawnPattern by patternGroup.pattern(
        "despawn",
        "§cYou have despawned your (monster|monsters)!"
    )
    private val healthPattern by patternGroup.pattern( //§a§ohannibal2's Tank Zombie§r §a160k§c❤
        "health",
        "§a§o(.+)'s (.+)§r §[ae]([\\dkm]+)§c❤"
    )
    private val seraphRecallPattern by patternGroup.pattern( //§cThe Seraph recalled your 3 summoned allies!
        "seraphrecall",
        "§cThe Seraph recalled your (\\d) summoned allies!"
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!SkyBlockAPI.isConnected) return

        val message = event.message
        spawnPattern.matchMatcher(message) {
            if (config.summoningMobDisplay) {
                event.blockedReason = "summoning_soul"
            }
            summoningsSpawned++
            searchArmorStands = true
            searchMobs = true
        }

        if (despawnPattern.matcher(message).matches() || message.startsWith("§c ☠ §r§7You ")) {
            despawned()
            if (config.summoningMobDisplay && !message.contains("☠")) {
                event.blockedReason = "summoning_soul"
            }
        }
        if (message == "§cThe Seraph recalled your summoned ally!" || seraphRecallPattern.matcher(message).matches()) {
            despawned()
            if (config.summoningMobDisplay) {
                event.blockedReason = "summoning_soul"
            }
        }
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        if (config.summoningMobDisplay && event.repeatSeconds(1)) {
            updateData()
        }

        if (searchArmorStands) {
            McWorld.getEntitiesOf<EntityArmorStand>().filter { it !in summoningMobNametags }
                .forEach {
                    val name = it.displayName.unformattedText
                    healthPattern.matchMatcher(name) {
                        if (name.contains(McPlayer.name)) {
                            summoningMobNametags.add(it)
                            if (summoningMobNametags.size == summoningsSpawned) {
                                searchArmorStands = false
                            }
                        }
                    }
                }
        }

        if (searchMobs) {
            val playerLocation = LocationUtils.playerLocation()
            McWorld.getEntitiesOf<EntityLiving>().filter {
                it !in summoningMobs.keys && it.getLorenzVec()
                    .distance(playerLocation) < 10 && it.ticksExisted < 2
            }.forEach {
                summoningMobs[it] = SummoningMob(System.currentTimeMillis(), name = "Mob")
                it.setColor(LorenzColor.GREEN)
                updateData()
                if (summoningMobs.size == summoningsSpawned) {
                    searchMobs = false
                }
            }
        }
    }

    private fun updateData() {
        if (summoningMobs.isEmpty()) return

        for (entry in HashMap(summoningMobs)) {
            val entityLiving = entry.key
            val summoningMob = entry.value

            val currentHealth = entityLiving.health.toInt()
            val name = summoningMob.name
            if (currentHealth == 0) {
                summoningMobs.remove(entityLiving)
                entityLiving.setColor(null)
                ChatUtils.chat("Your Summoning Mob just §cdied!")
                continue
            }

            val maxHealth = entityLiving.baseMaxHealth
            val color = NumberUtil.percentageColor(currentHealth.toLong(), maxHealth.toLong()).getChatColor()

            val currentFormat = NumberUtil.format(currentHealth)
            val maxFormat = NumberUtil.format(maxHealth)
            summoningMob.lastDisplayName = "§a$name $color$currentFormat/$maxFormat"
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.summoningMobDisplay) return
        if (summoningMobs.isEmpty()) return

        val list = mutableListOf<String>()
        list.add("Summoning mobs: " + summoningMobs.size)
        var id = 1
        for (mob in summoningMobs) {
            val name = mob.value.lastDisplayName
            list.add("#$id $name")
            id++
        }

        config.summoningMobDisplayPos.renderStrings(list, posLabel = "Summoning Mob Display")
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        despawned()
    }

    @HandleEvent(priority = HandleEvent.HIGH, generic = EntityArmorStand::class)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.summoningMobHideNametag) return

        val entity = event.entity
        if (!entity.hasCustomName()) return
        if (entity.isDead) return

        if (entity in summoningMobNametags) {
            event.cancel()
        }
    }

    private fun despawned() {
        summoningMobs.clear()
        summoningMobNametags.clear()
        summoningsSpawned = 0
        searchArmorStands = false
        searchMobs = false
    }

    private fun isEnabled(): Boolean {
        return SkyBlockAPI.isConnected && (config.summoningMobDisplay || config.summoningMobHideNametag)
    }

    class SummoningMob(
        val spawnTime: Long,
        var name: String = "",
        var lastDisplayName: String = "",
    )

    private infix fun EntityLivingBase.setColor(color: LorenzColor?) {
        if (color != null) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                this,
                color.toColor().withAlpha(127),
            ) { isEnabled() && config.summoningMobColored }
        } else {
            RenderLivingEntityHelper.removeCustomRender(this)
        }
    }
}
