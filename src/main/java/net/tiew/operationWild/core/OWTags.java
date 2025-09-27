package net.tiew.operationWild.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.tiew.operationWild.OperationWild;

public class OWTags {

    public static class Entities {
        public static final TagKey<EntityType<?>> FAWNS = tag("fawns");
        public static final TagKey<EntityType<?>> REPTILES = tag("reptiles");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> NEEDS_JADE_TOOL = tag("needs_jade_tool");
        public static final TagKey<Block> INCORRECT_FOR_JADE_TOOL = tag("incorrect_for_jade_tool");

        public static final TagKey<Block> NEEDS_RUBY_TOOL = tag("needs_ruby_tool");
        public static final TagKey<Block> INCORRECT_FOR_RUBY_TOOL = tag("incorrect_for_ruby_tool");

        public static final TagKey<Block> NEEDS_PRIMITIVE_TOOL = tag("needs_primitive_tool");
        public static final TagKey<Block> INCORRECT_FOR_PRIMITIVE_TOOL = tag("incorrect_for_primitive_tool");





        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
        }
    }

    public static class Items {

        public static final TagKey<Item> ARROWS = tag("arrows");

        public static final TagKey<Item> TIGER_TAMING_FOOD = tag("tiger_taming_food");
        public static final TagKey<Item> TIGER_SHARK_TAMING_FOOD = tag("tiger_shark_taming_food");

        public static final TagKey<Item> ACCEPT_PUTRID = tag("accept_putrid");
        public static final TagKey<Item> ACCEPT_EFFECTIVE_WEEDING = tag("accept_effective_weeding");
        public static final TagKey<Item> PRIMITIVE_TOOLS = tag("primitive_tools");
        public static final TagKey<Item> SLINGSHOT_PROJECTILES = tag("slingshot_projectiles");
        public static final TagKey<Item> BLOWPIPE_PROJECTILES = tag("blowpipe_projectiles");

        public static final TagKey<Item> KODIAK_FOOD = tag("kodiak_food");
        public static final TagKey<Item> KODIAK_DANGEROUS_FOOD = tag("kodiak_dangerous_food");


        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name));
        }
    }
}
