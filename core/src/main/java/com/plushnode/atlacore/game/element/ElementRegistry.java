package com.plushnode.atlacore.game.element;

import java.util.*;

public class ElementRegistry {
    private Set<Element> elements = new HashSet<>();

    public boolean registerElement(Element element) {
        this.elements.add(element);
        return true;
    }

    public Element getElementByName(String name) {
        for (Element element : elements) {
            if (element.getName().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    public List<Element> getElements() {
        return new ArrayList<>(elements);
    }

    public void clear() {
        this.elements.clear();
    }
}
