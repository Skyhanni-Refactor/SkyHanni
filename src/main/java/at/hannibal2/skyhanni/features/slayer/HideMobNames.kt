package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.item.EntityArmorStand
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object HideMobNames {

    private val lastMobName = TimeLimitedCache<Int, String>(2.minutes)
    private val mobNamesHidden = mutableListOf<Int>()
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
        val id = entity.entityId
        if (lastMobName.getOrNull(id) == name) {
            if (id in mobNamesHidden) {
                event.cancel()
            }
            return
        }

        lastMobName[id] = name
        mobNamesHidden.remove(id)

        if (shouldNameBeHidden(name)) {
            event.cancel()
            mobNamesHidden.add(id)
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
