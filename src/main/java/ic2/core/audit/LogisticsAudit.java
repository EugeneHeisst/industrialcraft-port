package ic2.core.audit;

import ic2.core.block.machine.tileentity.*;
import ic2.core.block.machine.container.ContainerLogistics;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.neoforge.capabilities.Capabilities;
import java.util.*;

public final class LogisticsAudit {
    public static void run(ServerLevel l,BlockPos p,CropAudit.Check check){
        for(var pos:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(7,2,2)))l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        l.setBlock(p,Ic2Blocks.SORTING_MACHINE.get().defaultBlockState(),3);
        l.setBlock(p.east(),Blocks.CHEST.defaultBlockState(),3);l.setBlock(p.west(),Blocks.CHEST.defaultBlockState(),3);
        var sort=(TileEntitySortingMachine)l.getBlockEntity(p);var east=(ChestBlockEntity)l.getBlockEntity(p.east());var west=(ChestBlockEntity)l.getBlockEntity(p.west());
        sort.setFilter(Direction.EAST.ordinal(),0,new ItemStack(Items.IRON_INGOT));sort.defaultRoute=Direction.WEST.ordinal();
        sort.buffer.put(0,new ItemStack(Items.IRON_INGOT,2));sort.buffer.put(1,new ItemStack(Items.GOLD_INGOT,2));sort.energy=100;
        sort.routeItems();
        check.test("sorter filters and default route",east.getItem(0).is(Items.IRON_INGOT)&&east.getItem(0).getCount()==1&&west.getItem(0).is(Items.GOLD_INGOT)&&west.getItem(0).getCount()==1&&sort.energy==60,"east="+east.getItem(0)+" west="+west.getItem(0)+" EU="+sort.energy);
        sort.energy=19;sort.routeItems();check.test("sorter insufficient EU retains input",sort.buffer.get(0).getCount()==1&&sort.energy==19,sort.buffer.get(0));
        for(int i=0;i<east.getContainerSize();i++)east.setItem(i,new ItemStack(Items.COBBLESTONE,64));
        sort.energy=100;sort.routeItems();
        check.test("sorter blocked whitelist never falls back",sort.buffer.get(0).is(Items.IRON_INGOT)&&west.getItem(0).getCount()==2,"retained="+sort.buffer.get(0)+" default="+west.getItem(0));
        var tag=sort.saveWithFullMetadata(l.registryAccess());var copy=(TileEntitySortingMachine)BlockEntity.loadStatic(p,sort.getBlockState(),tag,l.registryAccess());
        check.test("sorter filters and route persist",copy.defaultRoute==Direction.WEST.ordinal()&&copy.filters[Direction.EAST.ordinal()][0].is(Items.IRON_INGOT)&&copy.buffer.get(0).is(Items.IRON_INGOT),copy.filters[Direction.EAST.ordinal()][0]);
        var player=new ServerPlayer(l.getServer(),l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"LogisticsAudit"),ClientInformation.createDefault());
        var menu=new ContainerLogistics.Sorter(101,player.getInventory(),sort);menu.setCarried(new ItemStack(Items.DIAMOND,17));
        int ghost=menu.slots.size()-42;
        menu.clicked(ghost,0,ClickType.PICKUP,player);menu.clicked(ghost,0,ClickType.QUICK_MOVE,player);menu.clicked(ghost,0,ClickType.CLONE,player);
        check.test("sorter ghost filter never consumes or duplicates",menu.getCarried().getCount()==17&&sort.filters[0][0].is(Items.DIAMOND)&&sort.filters[0][0].getCount()==1,menu.getCarried());
        menu.clicked(ghost,1,ClickType.PICKUP,player);check.test("sorter ghost filter clear",sort.filters[0][0].isEmpty()&&menu.getCarried().getCount()==17,menu.getCarried());
        menu.onContainerEvent("3");menu.onContainerEvent("999");check.test("sorter default route button validates",sort.defaultRoute==3,sort.defaultRoute);
        var b=p.east(5);l.setBlock(b,Ic2Blocks.ITEM_BUFFER.get().defaultBlockState(),3);var buffer=(TileEntityItemBuffer)l.getBlockEntity(b);
        var vertical=l.getCapability(Capabilities.ItemHandler.BLOCK,b,Direction.UP);var horizontal=l.getCapability(Capabilities.ItemHandler.BLOCK,b,Direction.NORTH);
        var vrest=vertical.insertItem(0,new ItemStack(Items.IRON_INGOT,32),false);var hrest=horizontal.insertItem(0,new ItemStack(Items.GOLD_INGOT,16),false);
        check.test("item buffer isolated sided banks",vertical.getSlots()==24&&horizontal.getSlots()==24&&vrest.isEmpty()&&hrest.isEmpty()&&buffer.leftcontentSlot.get(0).is(Items.IRON_INGOT)&&buffer.rightcontentSlot.get(0).is(Items.GOLD_INGOT),"slots="+vertical.getSlots()+"/"+horizontal.getSlots());
        var bcopy=(TileEntityItemBuffer)BlockEntity.loadStatic(b,buffer.getBlockState(),buffer.saveWithFullMetadata(l.registryAccess()),l.registryAccess());
        check.test("item buffer both banks persist",bcopy.leftcontentSlot.get(0).getCount()==32&&bcopy.rightcontentSlot.get(0).getCount()==16,bcopy.getContainerSize());
        l.setBlock(b.north(),Blocks.CHEST.defaultBlockState(),3);l.setBlock(b.above(),Blocks.CHEST.defaultBlockState(),3);
        var leftUpgrade=new ItemStack(Ic2Items.EJECTOR_UPGRADE.get());leftUpgrade.set(Ic2Components.UPGRADE_DIR.get(),5);
        var rightUpgrade=new ItemStack(Ic2Items.EJECTOR_UPGRADE.get());rightUpgrade.set(Ic2Components.UPGRADE_DIR.get(),4);
        buffer.upgradeSlot.put(0,leftUpgrade);buffer.upgradeSlot.put(1,rightUpgrade);buffer.onLoaded();buffer.tick();buffer.tick();
        var north=(ChestBlockEntity)l.getBlockEntity(b.north());var up=(ChestBlockEntity)l.getBlockEntity(b.above());
        check.test("item buffer ejectors keep each bank independent",north.getItem(0).is(Items.IRON_INGOT)&&up.getItem(0).is(Items.GOLD_INGOT),"north="+north.getItem(0)+" up="+up.getItem(0));
        check.test("logistics crafting recipes",l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.SORTING_MACHINE.get().asItem()))&&l.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(l.registryAccess()).is(Ic2Blocks.ITEM_BUFFER.get().asItem())),"sorter + buffer");
    }
}
