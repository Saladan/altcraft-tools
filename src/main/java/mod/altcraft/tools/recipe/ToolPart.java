package mod.altcraft.tools.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mod.altcraft.tools.AltcraftTools;
import mod.altcraft.tools.handle.Handle;
import mod.altcraft.tools.item.AltcraftHandledItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class ToolPart implements Predicate<ItemStack> {

	public static final String JSON_IDENTIFIER = AltcraftTools.NAMESPACE + ":toolpart";
	public static final ToolPart NONE = new ToolPart(new Identifier(AltcraftTools.NAMESPACE, "none"), new ItemStack(Items.WOODEN_AXE));
	private final Identifier registry;
	private final ItemStack stack;
	private final AltcraftHandledItem item;
	private Ingredient ingredient;

	private ToolPart(Identifier registry, ItemStack stack) {
		this.registry = registry;
		this.stack = stack;
		this.item = (AltcraftHandledItem) stack.getItem();
		this.ingredient = null;
	}

	private Ingredient getCachedIngredient() {
		if (this.ingredient == null) {
			List<ItemStack> valid = Lists.newArrayList();
			for (Handle handle : this.item.getValidHandles()) {
				valid.addAll(Arrays.asList(handle.getIngredient().getIds().stream().map(RecipeFinder::getStackFromId).toArray(size -> new ItemStack[size])));
			}
			List<Item> items = Lists.newArrayList();
			for (ItemStack stack : valid) {
				if (!(items.contains(stack.getItem()))) {
					items.add(stack.getItem());
				}
			}
			Item[] itemArray = new Item[items.size()];
			for (int i = 0; i < items.size(); ++i) {
				itemArray[i] = items.get(i);
			}
			this.ingredient = Ingredient.ofItems(itemArray);
		}
		return this.ingredient;
	}

	@Override
	public boolean test(ItemStack stack) {
		return getCachedIngredient().test(stack);
	}

	public Ingredient getIngredient() {
		return getCachedIngredient();
	}

	public void addTag(ItemStack output, ItemStack material) {
		for (Handle handle : this.item.getValidHandles()) {
			if (handle.getIngredient().test(material)) {
				handle.addData(output);
				return;
			}
		}
	}

	public void write(PacketByteBuf buf) {
		buf.writeIdentifier(this.registry);
		buf.writeItemStack(this.stack);
	}

	public static ToolPart fromIdentifier(Identifier registry, ItemStack stack) {
		return new ToolPart(registry, stack);
	}

	public static ToolPart fromPacket(PacketByteBuf buf) {
		return fromIdentifier(buf.readIdentifier(), buf.readItemStack());
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ToolPart)) {
			return false;
		}
		return this.registry.equals(((ToolPart) other).registry) && this.stack.equals(((ToolPart) other).stack);
	}

}
