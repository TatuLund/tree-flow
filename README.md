[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/tree)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/tree.svg)](https://vaadin.com/directory/component/tree)

# Tree

Vaadin 14 Java version of Tree component

## Release notes

### Version 1.2.0

- Added support for image icons

### Version 1.1.1

- Added workaround for focus(), see https://github.com/vaadin/vaadin-grid-flow/issues/634 

### Version 1.1.0

- Added missing asMultiSelect() method
- Added setSizeByRows(..)
- Fixed sizing logic

### Version 1.0.0

- Initial version


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
