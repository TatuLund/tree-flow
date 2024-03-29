[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/tree)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/tree.svg)](https://vaadin.com/directory/component/tree)

# Tree

Vaadin 24 Java version of Tree component

## Release notes

### Version 3.2.0

- Add setSelectOnlyLeafs 
- Removed iron-icon use, as vaadin-icon supports Image icons from Vaadin 24.2+
- Vaadin 24.2+ required

### Version 3.1.0

- Add getDataCommunicator pass thru API

### Version 3.0.1

- Small fix in icon rendering

### Version 3.0.0

- Feature set is derived from version 2.4.0, but adopted for Vaadin 24
- Instead of title attribute vaadin-tooltip used for tooltips
- setTitleProvider renamed to setTooltipProvider
- getTitleProvider renamed to getTooltipProvider
- setHeightByRows renamed to setAllRowsVisible

## Development instructions

Starting the test/demo server:
1. Run `mvn jetty:run`.
2. Open http://localhost:8080 in the browser.

## Publishing to Vaadin Directory

You can create the zip package needed for [Vaadin Directory](https://vaadin.com/directory/) using
```
mvn versions:set -DnewVersion=1.0.0 # You cannot publish snapshot versions 
mvn install -Pdirectory
```

The package is created as `target/tree-flow-1.0.0.zip`

For more information or to upload the package, visit https://vaadin.com/directory/my-components?uploadNewComponent
