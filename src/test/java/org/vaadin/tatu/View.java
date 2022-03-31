package org.vaadin.tatu;

import java.util.Collections;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route("")
public class View extends Div {

    public View() {
        DepartmentData departmentData = new DepartmentData();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid Basics
        Tree<Department> tree = new Tree<>(Department::getName);

        tree.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);

        tree.setItemIconProvider(item -> getIcon(item));
        tree.setItemIconSrcProvider(item -> getImageIconSrc(item));
        tree.setItemTitleProvider(Department::getManager);

        tree.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        tree.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        tree.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null)
                System.out.println(event.getValue().getName() + " selected");
        });
        tree.setHeightByRows(true);

        // end-source-example
        tree.setId("treegridbasic");
        setSizeFull();
        tree.addItemClickListener(event -> {
            Notification.show("Click "+event.getItem().getName());
        });

        CheckboxGroup<GridVariant> variants = new CheckboxGroup<>();
        variants.setItems(GridVariant.values());
        variants.addValueChangeListener(event -> {
            tree.removeThemeVariants(GridVariant.values());
            variants.getValue().forEach(variant -> tree.addThemeVariants(variant));
        });

        TextField height = new TextField("Icon height");
        height.addValueChangeListener(event -> {
            tree.getElement().getStyle().set("--tree-icon-height",event.getValue());
        });

        add(withTreeToggleButtons(departmentData.getRootDepartments().get(0),
                tree, message, variants, height));
        
        Tree<Department> treeWithoutIconSrcProvider = new Tree<>(
                Department::getName);
        treeWithoutIconSrcProvider.setHeightByRows(true);
        treeWithoutIconSrcProvider.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeWithoutIconSrcProvider.setItemIconProvider(item -> getIcon(item));
        add(treeWithoutIconSrcProvider);
        
        Tree<Department> treeWithHtmlProvider = new Tree<>(
                Department::getName);
        treeWithHtmlProvider.setHeightByRows(true);
        treeWithHtmlProvider.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeWithHtmlProvider.setHtmlProvider(item -> "<b style=\"steelblue: red\">"+item.getName()+":</b> <i style=\"color: brown\">"+item.getManager()+"</i>");
        add(treeWithHtmlProvider);
    }

    private StreamResource getImageIconSrc(Department item) {
        if (item.getName().equalsIgnoreCase("vaadin")) {
            return new StreamResource("vaadin.svg", () -> getClass()
                    .getClassLoader().getResourceAsStream("images/vaadin.svg"));
        } else {
            return null;
        }
    }

    private VaadinIcon getIcon(Department item) {
        if (item.getParent() == null)
            return VaadinIcon.BUILDING;
        else if (item.getName().equalsIgnoreCase("vaadin")) {
            return null;
        } else
            return VaadinIcon.USER;
    }

    private <T> Component[] withTreeToggleButtons(T root, Tree<T> tree,
            Component... other) {
        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                evt -> {
                    if (tree.isExpanded(root)) {
                        tree.collapseRecursively(Collections.singleton(root),
                                0);
                    } else {
                        tree.expandRecursively(Collections.singleton(root), 0);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item");
        Div div1 = new Div(toggleFirstItem);

        NativeButton toggleRecursivelyFirstItem = new NativeButton(
                "Toggle first item recursively", evt -> {
                    if (tree.isExpanded(root)) {
                        tree.collapseRecursively(Collections.singleton(root),
                                2);
                    } else {
                        tree.expandRecursively(Collections.singleton(root), 2);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item-recur");
        Div div3 = new Div(toggleRecursivelyFirstItem);

        return Stream.concat(Stream.of(tree, div1, div3), Stream.of(other))
                .toArray(Component[]::new);
    }

}
