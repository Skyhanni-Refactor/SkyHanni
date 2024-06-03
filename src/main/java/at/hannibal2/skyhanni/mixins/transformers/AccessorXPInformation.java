package at.hannibal2.skyhanni.mixins.transformers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.XPInformation$SkillInfo")
public interface AccessorXPInformation {

    @Accessor(value = "totalXp")
    double getTotalXp();

}
