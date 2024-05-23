package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.entity.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.hasMaxHealth
import at.hannibal2.skyhanni.utils.EntityUtils.hasNameTagWith
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityGuardian
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.player.EntityPlayer
import kotlin.time.Duration.Companion.seconds

object SeaCreatureFeatures {

    private val config get() = SkyHanniMod.feature.fishing.rareCatches
    private var rareSeaCreatures = listOf<EntityLivingBase>()
    private var lastRareCatch = SimpleTimeMark.farPast()

    @HandleEvent
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!isEnabled()) return
        val entity = event.entity as? EntityLivingBase ?: return
        if (DamageIndicatorManager.isBoss(entity)) return

        val maxHealth = event.actualMaxHealth
        for (creatureType in RareSeaCreatureType.entries) {
            if (!creatureType.health.any { entity.hasMaxHealth(it, false, maxHealth) }) continue
            if (!creatureType.clazz.isInstance(entity)) continue
            if (!entity.hasNameTagWith(3, creatureType.nametag)) continue

            rareSeaCreatures = rareSeaCreatures.editCopy { add(entity) }
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, LorenzColor.RED.toColor().withAlpha(50))
            { config.highlight }

            // Water hydra splitting in two
            if (creatureType == RareSeaCreatureType.WATER_HYDRA && entity.health == (entity.baseMaxHealth.toFloat() / 2)) continue

            if (config.alertOtherCatches && lastRareCatch.passedSince() > 1.seconds) {
                val creature = SeaCreatureManager.allFishingMobs[creatureType.nametag]
                val text = "${creature?.rarity?.chatColorCode ?: "§6"}RARE SEA CREATURE!"
                TitleManager.sendTitle(text, 1.5.seconds, 3.6, 7f)
                if (config.playSound) McSound.BEEP.play()
            }
        }
    }

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.alertOwnCatches) return

        if (event.seaCreature.rare) {
            TitleManager.sendTitle("${event.seaCreature.rarity.chatColorCode}RARE CATCH!", 3.seconds, 2.8, 7f)
            if (config.playSound) McSound.BEEP.play()
            lastRareCatch = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        rareSeaCreatures = emptyList()
    }

    @HandleEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (isEnabled() && config.highlight && event.type === RenderEntityOutlineEvent.Type.XRAY) {
            event.queueEntitiesToOutline(getEntityOutlineColor)
        }
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && !DungeonAPI.inDungeon() && !KuudraAPI.inKuudra

    private val getEntityOutlineColor: (entity: Entity) -> Int? = { entity ->
        if (entity is EntityLivingBase && entity in rareSeaCreatures && entity.distanceToPlayer() < 30) {
            LorenzColor.GREEN.toColor().rgb
        } else null
    }

    enum class RareSeaCreatureType(
        val clazz: Class<out EntityLivingBase>,
        val nametag: String,
        vararg val health: Int,
    ) {

        WATER_HYDRA(EntityZombie::class.java, "Water Hydra", 500_000),
        SEA_EMPEROR(EntityGuardian::class.java, "Sea Emperor", 750_000, 800_000),
        SEA_EMPEROR_RIDER(EntitySkeleton::class.java, "Sea Emperor", 750_000, 800_000),
        ZOMBIE_MINER(EntityPlayer::class.java, "Zombie Miner", 2_000_000),
        PHANTOM_FISHERMAN(EntityPlayer::class.java, "Phantom Fisher", 1_000_000),
        GRIM_REAPER(EntityPlayer::class.java, "Grim Reaper", 3_000_000),
        YETI(EntityPlayer::class.java, "Yeti", 2_000_000),
        NUTCRACKER(EntityZombie::class.java, "Nutcracker", 4_000_000),
        GREAT_WHITE_SHARK(EntityPlayer::class.java, "Great White Shark", 1_500_000),
        THUNDER(EntityGuardian::class.java, "Thunder", 35_000_000),
        LORD_JAWBUS(EntityIronGolem::class.java, "Lord Jawbus", 100_000_000),
        PLHLEGBLAST(EntitySquid::class.java, "Plhlegblast", 500_000_000),
        ;
    }
}
