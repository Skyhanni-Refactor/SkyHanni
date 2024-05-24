package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import net.minecraft.entity.item.EntityArmorStand
import java.util.regex.Pattern

object HideMobNames {

    private val lastMobName = mutableMapOf<EntityArmorStand, String>()
    private val mobNamesHidden = mutableListOf<EntityArmorStand>()
    private val patterns = mutableListOf<Pattern>()

    private fun addMobToHide(bossName: String) {
        patterns.add("§8\\[§7Lv\\d+§8] §c$bossName§r §[ae](?<min>.+)§f/§a(?<max>.+)§c❤".toPattern())
    }

    @HandleEvent
    fun onRepoLoad(event: RepositoryReloadEvent) {
        patterns.clear()
        event.getConstant<Array<String>>("MobToHide").forEach { addMobToHide(it) }
    }
    
    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGH, generic = EntityArmorStand::class)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!SkyHanniMod.feature.slayer.hideMobNames) return

        val entity = event.entity
        if (!entity.hasCustomName()) return

        val name = entity.name
        if (lastMobName.getOrDefault(entity, "abc") == name) {
            if (entity in mobNamesHidden) {
                event.cancel()
            }
            return
        }

        lastMobName[entity] = name
        mobNamesHidden.remove(entity)

        if (shouldNameBeHidden(name)) {
            event.cancel()
            mobNamesHidden.add(entity)
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        lastMobName.clear()
        mobNamesHidden.clear()
    }

    private fun shouldNameBeHidden(name: String): Boolean {
        for (pattern in patterns) {
            pattern.matchMatcher(name) {
                val min = group("min")
                val max = group("max")
                if (min == max || min == "0") {
                    return true
                }
            }
        }

        return false
    }
}
