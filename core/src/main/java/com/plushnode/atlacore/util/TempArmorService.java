package com.plushnode.atlacore.util;

import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TempArmorService {
    private List<TempArmor> tempArmors = new ArrayList<>();

    public List<TempArmor> getTempArmors(User user) {
        return tempArmors.stream()
                .filter(tempArmor -> tempArmor.getUser().equals(user))
                .collect(Collectors.toList());
    }

    public void add(User user, ItemStack newItem, TempArmor.Slot slot, long duration) {
        revert(user, slot);
        this.tempArmors.add(new TempArmor(user, newItem, slot, duration));
    }

    public void reload() {
        for (TempArmor tempArmor : tempArmors) {
            tempArmor.getTask().cancel();
            tempArmor.revert();
        }

        tempArmors.clear();
    }

    public void revert(User user, TempArmor.Slot slot) {
        for (Iterator<TempArmor> iter = tempArmors.iterator(); iter.hasNext();) {
            TempArmor tempArmor = iter.next();

            if (tempArmor.getUser().equals(user) && tempArmor.getSlot() == slot) {
                tempArmor.getTask().cancel();
                tempArmor.revert();
                iter.remove();
            }
        }
    }

    public void revert(User user) {
        for (Iterator<TempArmor> iter = tempArmors.iterator(); iter.hasNext();) {
            TempArmor tempArmor = iter.next();

            if (tempArmor.getUser().equals(user)) {
                tempArmor.getTask().cancel();
                tempArmor.revert();
                iter.remove();
            }
        }
    }
}
