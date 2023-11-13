package org.vaadin.tatu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.treegrid.CollapseEvent;
import com.vaadin.flow.component.treegrid.ExpandEvent;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.hierarchy.HasHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

/**
 * Tree component. A Tree can be used to select an item from a hierarchical set
 * of items.
 *
 * @author Tatu Lund
 *
 * @param <T>
 *            the data type
 */
// @CssImport(value = "./tree-styles.css", themeFor = "vaadin-grid")
@CssImport(value = "./grid-tree-toggle-adjust.css", themeFor = "vaadin-grid-tree-toggle")
public class Tree<T> extends Composite<Div>
        implements HasHierarchicalDataProvider<T>, Focusable, HasComponents,
        HasSize, HasElement, HasTheme {

    private class CustomizedTreeGrid<T> extends TreeGrid<T> {
        private final List<StreamRegistration> registrations = new ArrayList<>();

        private Column<T> setHierarchyColumn(
                ValueProvider<T, ?> valueProvider) {
            Column<T> column = addColumn(TemplateRenderer
                    .<T> of("<vaadin-grid-tree-toggle "
                            + "theme$='[[item.theme]]' "
                            + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                            + "[[item.name]]" + "</vaadin-grid-tree-toggle>")
                    .withProperty("theme", item -> getThemeName())
                    .withProperty("leaf",
                            item -> !getDataCommunicator().hasChildren(item))
                    .withProperty("name", value -> String
                            .valueOf(valueProvider.apply(value))));
            final SerializableComparator<T> comparator = (a,
                    b) -> compareMaybeComparables(valueProvider.apply(a),
                            valueProvider.apply(b));
            column.setComparator(comparator);

            return column;
        }

        private Column<T> setHierarchyColumnWithHtml(
                ValueProvider<T, ?> valueProvider) {
            Column<T> column = addColumn(TemplateRenderer
                    .<T> of("<vaadin-grid-tree-toggle "
                            + "theme$='[[item.theme]]' "
                            + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]' inner-h-t-m-l=\"[[item.html]]\">"
                            + "</vaadin-grid-tree-toggle>")
                    .withProperty("theme", item -> Tree.this.getThemeName())
                    .withProperty("leaf",
                            item -> !getDataCommunicator().hasChildren(item))
                    .withProperty("html", value -> sanitize(
                            String.valueOf(valueProvider.apply(value)))));
            final SerializableComparator<T> comparator = (a,
                    b) -> compareMaybeComparables(valueProvider.apply(a),
                            valueProvider.apply(b));
            column.setComparator(comparator);

            return column;
        }

        private Column<T> setHierarchyColumnWithIcon(
                ValueProvider<T, ?> valueProvider,
                ValueProvider<T, VaadinIcon> iconProvider,
                ValueProvider<T, StreamResource> iconSrcProvider) {
            Column<T> column = addColumn(TemplateRenderer
                    .<T> of("<vaadin-grid-tree-toggle "
                            + "theme$='[[item.theme]]' "
                            + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                            + "<iron-icon src='[[item.iconSrc]]' icon='[[item.icon]]' style='height: var(--tree-icon-height, 15px); padding-right: 10px'></iron-icon>"
                            + "[[item.name]]" + "</vaadin-grid-tree-toggle>")
                    .withProperty("theme", item -> Tree.this.getThemeName())
                    .withProperty("leaf",
                            item -> !getDataCommunicator().hasChildren(item))
                    .withProperty("icon",
                            icon -> iconProvider == null ? null
                                    : getIcon(iconProvider, icon))
                    .withProperty("iconSrc",
                            icon -> iconSrcProvider == null ? null
                                    : getIconSrc(iconSrcProvider, icon))
                    .withProperty("name", value -> String
                            .valueOf(valueProvider.apply(value))));
            final SerializableComparator<T> comparator = (a,
                    b) -> compareMaybeComparables(valueProvider.apply(a),
                            valueProvider.apply(b));
            column.setComparator(comparator);

            return column;
        }

        private Column<T> setHierarchyColumnWithTitle(
                ValueProvider<T, ?> valueProvider,
                ValueProvider<T, String> titleProvider) {
            Column<T> column = addColumn(TemplateRenderer
                    .<T> of("<vaadin-grid-tree-toggle title='[[item.title]]'"
                            + "theme$='[[item.theme]]' "
                            + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                            + "[[item.name]]" + "</vaadin-grid-tree-toggle>")
                    .withProperty("theme", item -> Tree.this.getThemeName())
                    .withProperty("leaf",
                            item -> !getDataCommunicator().hasChildren(item))
                    .withProperty("title",
                            title -> String.valueOf(titleProvider.apply(title)))
                    .withProperty("name", value -> String
                            .valueOf(valueProvider.apply(value))));
            final SerializableComparator<T> comparator = (a,
                    b) -> compareMaybeComparables(valueProvider.apply(a),
                            valueProvider.apply(b));
            column.setComparator(comparator);

            return column;
        }

        public Column<T> setHierarchyColumn(ValueProvider<T, ?> valueProvider,
                ValueProvider<T, VaadinIcon> iconProvider,
                ValueProvider<T, String> titleProvider) {
            return setHierarchyColumn(valueProvider, iconProvider, null,
                    titleProvider);
        }

        public Column<T> setHierarchyColumn(ValueProvider<T, ?> valueProvider,
                ValueProvider<T, VaadinIcon> iconProvider,
                ValueProvider<T, StreamResource> iconSrcProvider,
                ValueProvider<T, String> titleProvider) {
            removeAllColumns();
            Column<T> column;
            if ((iconProvider == null && iconSrcProvider == null)
                    && (titleProvider == null)) {
                column = setHierarchyColumn(valueProvider);
            } else if ((iconProvider != null || iconSrcProvider != null)
                    && (titleProvider == null)) {
                column = setHierarchyColumnWithIcon(valueProvider, iconProvider,
                        iconSrcProvider);
            } else if ((iconProvider == null && iconSrcProvider == null)
                    && (titleProvider != null)) {
                column = setHierarchyColumnWithTitle(valueProvider,
                        titleProvider);
            } else {
                column = addColumn(TemplateRenderer.<T> of(
                        "<vaadin-grid-tree-toggle title='[[item.title]]'"
                                + "theme$='[[item.theme]]' "
                                + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                                + "<iron-icon src='[[item.iconSrc]]' icon='[[item.icon]]' style='height: var(--tree-icon-height, 15px); padding-right: 10px'></iron-icon>"
                                + "[[item.name]]"
                                + "</vaadin-grid-tree-toggle>")
                        .withProperty("theme", item -> Tree.this.getThemeName())
                        .withProperty("leaf",
                                item -> !getDataCommunicator()
                                        .hasChildren(item))
                        .withProperty("title",
                                title -> String
                                        .valueOf(titleProvider.apply(title)))
                        .withProperty("icon",
                                icon -> iconProvider == null ? null
                                        : getIcon(iconProvider, icon))
                        .withProperty("iconSrc",
                                icon -> iconSrcProvider == null ? null
                                        : getIconSrc(iconSrcProvider, icon))
                        .withProperty("name", value -> String
                                .valueOf(valueProvider.apply(value))));
                final SerializableComparator<T> comparator = (a,
                        b) -> compareMaybeComparables(valueProvider.apply(a),
                                valueProvider.apply(b));
                column.setComparator(comparator);
            }

            return column;
        }

        private String getIconSrc(
                ValueProvider<T, StreamResource> iconSrcProvider, T icon) {
            StreamResource streamResource = iconSrcProvider.apply(icon);
            if (streamResource == null) {
                return null;
            } else {
                StreamResourceRegistry resourceRegistry = VaadinSession
                        .getCurrent().getResourceRegistry();
                registrations
                        .add(resourceRegistry.registerResource(streamResource));
                return resourceRegistry.getTargetURI(streamResource).toString();
            }
        }

        private String getIcon(ValueProvider<T, VaadinIcon> iconProvider,
                T icon) {
            VaadinIcon vaadinIcon = iconProvider.apply(icon);
            if (vaadinIcon == null) {
                return null;
            } else {
                return "vaadin:" + fixIconName(String.valueOf(vaadinIcon));
            }
        }

        private String fixIconName(String name) {
            String trimmed;
            trimmed = name.toLowerCase();
            trimmed = trimmed.replace("_", "-");
            return trimmed;
        }

        @Override
        protected void onDetach(DetachEvent detachEvent) {
            registrations.forEach(StreamRegistration::unregister);
            super.onDetach(detachEvent);
        }
    }

    private CustomizedTreeGrid<T> treeGrid = createTreeGrid();

    /**
     * Create inner {@link TreeGrid} object. May be overridden in subclasses.
     *
     * @return new {@link TreeGrid}
     */
    protected CustomizedTreeGrid<T> createTreeGrid() {
        return new CustomizedTreeGrid<>();
    }

    private ValueProvider<T, VaadinIcon> iconProvider;
    private ValueProvider<T, StreamResource> iconSrcProvider;
    private ValueProvider<T, ?> valueProvider;
    private ValueProvider<T, String> titleProvider;
    private boolean sanitize = true;

    /**
     * Constructs a new Tree Component.
     * 
     * @param valueProvider
     *            the item caption provider to use, not <code>null</code>
     */
    public Tree(ValueProvider<T, ?> valueProvider) {
        this.valueProvider = valueProvider;
        treeGrid.setHierarchyColumn(valueProvider, iconProvider,
                iconSrcProvider, titleProvider);
        treeGrid.setSelectionMode(SelectionMode.SINGLE);
        treeGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);

        treeGrid.setSizeFull();
        treeGrid.addClassName("tree");
        add(treeGrid);
    }

    /**
     * Constructs a new Tree Component with given caption and {@code TreeData}.
     *
     * @param valueProvider
     *            the item caption provider to use, not <code>null</code>
     * @param treeData
     *            the tree data for component
     */
    public Tree(TreeData<T> treeData, ValueProvider<T, ?> valueProvider) {
        this(new TreeDataProvider<>(treeData), valueProvider);
    }

    /**
     * Constructs a new Tree Component with given caption and
     * {@code HierarchicalDataProvider}.
     *
     * @param valueProvider
     *            the item caption provider to use, not <code>null</code>
     * @param dataProvider
     *            the hierarchical data provider for component
     */
    public Tree(HierarchicalDataProvider<T, ?> dataProvider,
            ValueProvider<T, ?> valueProvider) {
        this(valueProvider);

        treeGrid.setDataProvider(dataProvider);
    }

    @Override
    public HierarchicalDataProvider<T, SerializablePredicate<T>> getDataProvider() {
        return treeGrid.getDataProvider();
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        treeGrid.setDataProvider(dataProvider);
    }

    /**
     * Adds an ExpandListener to this Tree.
     *
     * @see ExpandEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    public Registration addExpandListener(
            ComponentEventListener<ExpandEvent<T, TreeGrid<T>>> listener) {
        return treeGrid.addExpandListener(listener);
    }

    /**
     * Adds a CollapseListener to this Tree.
     *
     * @see CollapseEvent
     *
     * @param listener
     *            the listener to add
     * @return a registration for the listener
     */
    public Registration addCollapseListener(
            ComponentEventListener<CollapseEvent<T, TreeGrid<T>>> listener) {
        return treeGrid.addCollapseListener(listener);
    }

    /**
     * Fires an expand event with given item.
     *
     * @param collection
     *            the expanded item
     * @param userOriginated
     *            whether the expand was triggered by a user interaction or the
     *            server
     */
    protected void fireExpandEvent(Collection<T> collection,
            boolean userOriginated) {
        fireEvent(new ExpandEvent<>(this, userOriginated, collection));
    }

    /**
     * Fires a collapse event with given item.
     *
     * @param collection
     *            the collapsed item
     * @param userOriginated
     *            whether the collapse was triggered by a user interaction or
     *            the server
     */
    protected void fireCollapseEvent(Collection<T> collection,
            boolean userOriginated) {
        fireEvent(new CollapseEvent<>(this, userOriginated, collection));
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    public void expand(T... items) {
        treeGrid.expand(items);
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have
     * any children, does nothing.
     *
     * @param items
     *            the items to expand
     */
    public void expand(Collection<T> items) {
        treeGrid.expand(items);
    }

    /**
     * Expands the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code expandRecursively(items, 0)} expands only
     * the given items while {@code expandRecursively(items, 2)} expands the
     * given items as well as their children and grandchildren.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     */
    public void expandRecursively(Collection<T> items, int depth) {
        treeGrid.expandRecursively(items, depth);
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    public void collapse(T... items) {
        treeGrid.collapse(items);
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items
     *            the collection of items to collapse
     */
    public void collapse(Collection<T> items) {
        treeGrid.collapse(items);
    }

    /**
     * Collapse the given items and their children recursively until the given
     * depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     *
     * @param items
     *            the items to expand recursively
     * @param depth
     *            the maximum depth of recursion
     */
    public void collapseRecursively(Collection<T> items, int depth) {
        treeGrid.collapseRecursively(items, depth);
    }

    /**
     * Returns whether a given item is expanded or collapsed.
     *
     * @param item
     *            the item to check
     * @return true if the item is expanded, false if collapsed
     */
    public boolean isExpanded(T item) {
        return treeGrid.isExpanded(item);
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @see #getSelectionModel()
     *
     * @return set of selected items
     */
    public Set<T> getSelectedItems() {
        return treeGrid.getSelectedItems();
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @param item
     *            item to select
     *
     * @see SelectionModel#select(Object)
     * @see #getSelectionModel()
     */
    public void select(T item) {
        treeGrid.select(item);
    }

    /**
     * This method is a shorthand that delegates to the currently set selection
     * model.
     *
     * @param item
     *            item to deselect
     *
     * @see SelectionModel#deselect(Object)
     * @see #getSelectionModel()
     */
    public void deselect(T item) {
        treeGrid.deselect(item);
    }

    /**
     * Adds a selection listener to the current selection model.
     * <p>
     * <strong>NOTE:</strong> If selection mode is switched with
     * {@link #setSelectionMode(SelectionMode)}, then this listener is not
     * triggered anymore when selection changes!
     *
     * @param listener
     *            the listener to add
     * @return a registration handle to remove the listener
     *
     * @throws UnsupportedOperationException
     *             if selection has been disabled with
     *             {@link SelectionMode#NONE}
     */
    public Registration addSelectionListener(
            SelectionListener<Grid<T>, T> listener) {
        return treeGrid.addSelectionListener(listener);
    }

    /**
     * Use this tree as a multi select in Binder. Throws
     * {@link IllegalStateException} if the tree is not using
     * {@link SelectionMode#MULTI}.
     *
     * @return the single select wrapper that can be used in binder
     */
    public MultiSelect<Grid<T>, T> asMultiSelect() {
        return treeGrid.asMultiSelect();
    }

    /**
     * Use this tree as a single select in Binder. Throws
     * {@link IllegalStateException} if the tree is not using
     * {@link SelectionMode#SINGLE}.
     *
     * @return the single select wrapper that can be used in binder
     */
    public SingleSelect<Grid<T>, T> asSingleSelect() {
        return treeGrid.asSingleSelect();
    }

    /**
     * Returns the selection model for this Tree.
     *
     * @return the selection model, not <code>null</code>
     */
    public GridSelectionModel<T> getSelectionModel() {
        return treeGrid.getSelectionModel();
    }

    /**
     * Sets the item generator that is used to produce the html content shown
     * for each item. By default, {@link String#valueOf(Object)} is
     * used.
     * <p>
     * Note: This will override icon, title and value provider settings.
     *
     * @param htmlProvider
     *            the item html provider to use, not <code>null</code>
     */
    public void setHtmlProvider(ValueProvider<T, ?> htmlProvider) {
        treeGrid.removeAllColumns();
        Objects.requireNonNull(valueProvider,
                "Caption generator must not be null");
        treeGrid.setHierarchyColumnWithHtml(htmlProvider);
        treeGrid.getDataCommunicator().reset();
    }

    /**
     * Sets the item caption generator that is used to produce the strings shown
     * as the text for each item. By default, {@link String#valueOf(Object)} is
     * used.
     *
     * @param valueProvider
     *            the item caption provider to use, not <code>null</code>
     */
    public void setItemCaptionProvider(ValueProvider<T, ?> valueProvider) {
        Objects.requireNonNull(valueProvider,
                "Caption generator must not be null");
        this.valueProvider = valueProvider;
        treeGrid.setHierarchyColumn(valueProvider, iconProvider,
                iconSrcProvider, titleProvider);
        treeGrid.getDataCommunicator().reset();
    }

    /**
     * Sets the item icon generator that is used to produce custom icons for
     * items. The generator can return <code>null</code> for items with no icon.
     *
     * @param iconProvider
     *            the item icon generator to set, not <code>null</code>
     * @throws NullPointerException
     *             if {@code itemIconGenerator} is {@code null}
     */
    public void setItemIconProvider(ValueProvider<T, VaadinIcon> iconProvider) {
        Objects.requireNonNull(iconProvider,
                "Item icon generator must not be null");
        this.iconProvider = iconProvider;
        treeGrid.setHierarchyColumn(valueProvider, iconProvider,
                iconSrcProvider, titleProvider);
        treeGrid.getDataCommunicator().reset();
    }

    /**
     * Sets the item icon src generator that is used to produce custom icons for
     * items. The generator can return <code>null</code> for items with no icon.
     *
     * @param iconSrcProvider
     *            the item icon generator to set, not <code>null</code>
     * @throws NullPointerException
     *             if {@code itemIconGenerator} is {@code null}
     */
    public void setItemIconSrcProvider(
            ValueProvider<T, StreamResource> iconSrcProvider) {
        Objects.requireNonNull(iconSrcProvider,
                "Item icon src generator must not be null");
        this.iconSrcProvider = iconSrcProvider;
        treeGrid.setHierarchyColumn(valueProvider, iconProvider,
                iconSrcProvider, titleProvider);
        treeGrid.getDataCommunicator().reset();
    }

    /**
     * Sets the style generator that is used for generating class names for
     * items in this tree. Returning null from the generator results in no
     * custom style name being set.
     *
     * @param classNameGenerator
     *            the item style generator to set, not {@code null}
     * @throws NullPointerException
     *             if {@code styleGenerator} is {@code null}
     */
    public void setClassNameGenerator(
            SerializableFunction<T, String> classNameGenerator) {
        treeGrid.setClassNameGenerator(classNameGenerator);
    }

    /**
     * Sets the title generator that is used for generating tooltip descriptions
     * for items.
     *
     * @param titleProvider
     *            the item description generator to set, or <code>null</code> to
     *            remove a previously set generator
     */
    public void setItemTitleProvider(ValueProvider<T, String> titleProvider) {
        this.titleProvider = titleProvider;
        treeGrid.setHierarchyColumn(valueProvider, iconProvider,
                iconSrcProvider, titleProvider);
        treeGrid.getDataCommunicator().reset();
    }

    /**
     * Gets the item caption provider.
     *
     * @return the item caption provider
     */
    public ValueProvider<T, ?> getItemCaptionProvider() {
        return this.valueProvider;
    }

    /**
     * Gets the item icon provider.
     *
     * @return the item icon provider
     */
    public ValueProvider<T, VaadinIcon> getIconProvider() {
        return iconProvider;
    }

    /**
     * Gets the item icon provider.
     *
     * @return the item icon provider
     */
    public ValueProvider<T, StreamResource> getIconSrcProvider() {
        return iconSrcProvider;
    }

    /**
     * Gets the class name generator.
     *
     * @return the item style generator
     */
    public SerializableFunction<T, String> getClassNameGenerator() {
        return treeGrid.getClassNameGenerator();
    }

    /**
     * Gets the item description generator.
     *
     * @return the item description generator
     */
    public ValueProvider<T, String> getTitleProvider() {
        return titleProvider;
    }

    /**
     * Adds an item click listener. The listener is called when an item of this
     * {@code Tree} is clicked.
     *
     * @param listener
     *            the item click listener, not null
     * @return a registration for the listener
     */
    public Registration addItemClickListener(
            ComponentEventListener<ItemClickEvent<T>> listener) {
        return treeGrid.addItemClickListener(listener);
    }

    /**
     * Sets the tree's selection mode.
     * <p>
     * The built-in selection modes are:
     * <ul>
     * <li>{@link SelectionMode#SINGLE} <b>the default model</b></li>
     * <li>{@link SelectionMode#MULTI}</li>
     * <li>{@link SelectionMode#NONE} preventing selection</li>
     * </ul>
     *
     * @param selectionMode
     *            the selection mode to switch to, not {@code null}
     * @return the used selection model
     *
     * @see SelectionMode
     */
    public GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode) {
        Objects.requireNonNull(selectionMode,
                "Can not set selection mode to null");
        return treeGrid.setSelectionMode(selectionMode);
    }

    private SelectionMode getSelectionMode() {
        GridSelectionModel<T> selectionModel = getSelectionModel();
        SelectionMode mode = null;
        if (selectionModel.getClass().equals(GridSingleSelectionModel.class)) {
            mode = SelectionMode.SINGLE;
        } else if (selectionModel.getClass()
                .equals(GridMultiSelectionModel.class)) {
            mode = SelectionMode.MULTI;
        } else {
            mode = SelectionMode.NONE;
        }
        return mode;
    }

    /**
     * Gets the CSS class names for this component.
     *
     * @return a space-separated string of class names, or <code>null</code> if
     *         there are no class names
     */
    public String getClassName() {
        return treeGrid.getClassName();
    }

    /**
     * Sets the CSS class names of this component. This method overwrites any
     * previous set class names.
     *
     * @param className
     *            a space-separated string of class names to set, or
     *            <code>null</code> to remove all class names
     */
    public void setClassName(String className) {
        treeGrid.setClassName(className);
    }

    /**
     * Removes a CSS class name from this component.
     *
     * @param className
     *            the CSS class name to remove, not <code>null</code>
     * @return <code>true</code> if the class name was removed,
     *         <code>false</code> if the class list didn't contain the class
     *         name
     */
    public boolean removeClassName(String className) {
        return treeGrid.removeClassName(className);
    }

    @Override
    public void setId(String id) {
        treeGrid.setId(id);
    }

    @Override
    public Optional<String> getId() {
        return treeGrid.getId();
    }

    public GridContextMenu<T> addContextMenu() {
        return treeGrid.addContextMenu();
    }

    /**
     * Scrolls to a certain item.
     * <p>
     * If the item has an open details row, its size will also be taken into
     * account.
     *
     * @param row
     *            zero based index of the item to scroll to in the current view.
     * @throws IllegalArgumentException
     *             if the provided row is outside the item range
     */
    public void scrollToIndex(int row) throws IllegalArgumentException {
        treeGrid.scrollToIndex(row);
    }

    /**
     * Scrolls to the beginning of the first data row.
     */
    public void scrollToStart() {
        treeGrid.scrollToStart();
    }

    /**
     * Scrolls to the end of the last data row.
     */
    public void scrollToEnd() {
        treeGrid.scrollToEnd();
    }

    @Override
    public int getTabIndex() {
        return treeGrid.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        treeGrid.setTabIndex(tabIndex);
    }

    @Override
    public void focus() {
        treeGrid.getElement().executeJs(
                "setTimeout(function(){let firstTd = $0.shadowRoot.querySelector('tr:first-child > td:first-child'); firstTd.click(); firstTd.focus(); },0)",
                treeGrid.getElement());
    }

    public void setHeightByRows(boolean heightByRows) {
        treeGrid.setHeightByRows(heightByRows);
    }

    public void addThemeVariants(GridVariant... gridVariants) {
        treeGrid.addThemeVariants(gridVariants);
    }

    public void removeThemeVariants(GridVariant... gridVariants) {
        treeGrid.removeThemeVariants(gridVariants);
    }

    /**
     * By default html content is sanitized, if you have custom web components
     * to or other offending content that you want to render, set to false.
     * 
     * @param sanitize
     *            Enable / disable html sanitation.
     */
    public void setSanitize(boolean sanitize) {
        this.sanitize  = sanitize;
    }

    @Override
    public void setThemeName(String theme) {
        treeGrid.setThemeName(theme);
    }

    @Override
    public String getThemeName() {
        return treeGrid.getElement().getAttribute("theme");
    }

    @Override 
    public ThemeList getThemeNames() {
        return treeGrid.getThemeNames();
    }

    private String sanitize(String html) {
        if (sanitize) {
        Safelist safelist = Safelist.relaxed()
                .addAttributes(":all", "style")
                .addEnforcedAttribute("a", "rel", "nofollow");
        String sanitized = Jsoup.clean(html, "", safelist,
                new Document.OutputSettings().prettyPrint(false));
        return sanitized;
        } else {
            return html;
        }
    }

    /**
     * Sets the drop mode of this drop target. When set to not {@code null},
     * tree fires drop events upon data drop over the tree or the tree rows.
     * <p>
     * When using {@link GridDropMode#ON_TOP}, and the tree is either empty or
     * has empty space after the last row, the drop can still happen on the
     * empty space, and the {@link GridDropEvent#getDropTargetItem()} will
     * return an empty optional.
     * <p>
     * When using {@link GridDropMode#BETWEEN} or
     * {@link GridDropMode#ON_TOP_OR_BETWEEN}, and there is at least one row in
     * the tree, any drop after the last row in the tree will get the last row
     * as the {@link GridDropEvent#getDropTargetItem()}. If there are no rows in
     * the tree, then it will return an empty optional.
     * <p>
     * If using {@link GridDropMode#ON_GRID}, then the drop will not happen on
     * any row, but instead just "on the tree". The target row will not be
     * present in this case.
     * <p>
     * NOTE: Prefer not using a row specific {@link GridDropMode} with a
     * tree that enables sorting. If for example a new row gets added to a
     * specific location on drop event, it might not end up in the location of
     * the drop but rather where the active sorting configuration prefers to
     * place it. This behavior might feel unexpected for the users.
     *
     * @param dropMode
     *            Drop mode that describes the allowed drop locations within the
     *            Tree's row. Can be {@code null} to disable dropping on the
     *            tree.
     * @see GridDropEvent#getDropLocation()
     */
    public void setDropMode(GridDropMode dropMode) {
        treeGrid.setDropMode(dropMode);
    }

    /**
     * Gets the drop mode of this drop target.
     *
     * @return Drop mode that describes the allowed drop locations within the
     *         Tree's row. {@code null} if dropping is not enabled.
     */
    public GridDropMode getDropMode() {
        return treeGrid.getDropMode();
    }

    /**
     * Sets whether the user can drag the tree rows or not.
     *
     * @param rowsDrraggable
     *            {@code true} if the rows can be dragged by the user;
     *            {@code false} if not
     */
    public void setRowsDraggable(boolean rowsDraggable) {
        treeGrid.setRowsDraggable(rowsDraggable);
    }

    /**
     * Gets whether rows of the tree can be dragged.
     *
     * @return {@code true} if the rows are draggable, {@code false} otherwise
     */

    public boolean isRowsDraggable() {
        return treeGrid.isRowsDraggable();
    }

    /**
     * Adds a drag start listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addDragStartListener(ComponentEventListener<GridDragStartEvent<T>> listener) {
        return treeGrid.addDragStartListener(listener);
    }

    /**
     * Adds a drop listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addDropListener(ComponentEventListener<GridDropEvent<T>> listener) {
        return treeGrid.addDropListener(listener);
    }

    /**
     * Adds a drag end listener to this component.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addDragEndListener(ComponentEventListener<GridDragEndEvent<T>> listener) {
        return treeGrid.addDragEndListener(listener);
    }
}
