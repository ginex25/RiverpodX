# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0](https://github.com/ginex25/RiverpodX/releases/tag/v1.3.0) - 2025-08-29

### Added

- Support for IntelliJ IDEA 2025.2 (build 252.*)

### Changed

- Updated `until-build` to `252.*` for compatibility with IntelliJ IDEA 2025.2.

## [1.2.0](https://github.com/ginex25/RiverpodX/releases/tag/v1.2.0) - 2025-05-03

### Added

- Support for converting `StatelessWidget` and `StatefulWidget` to `ConsumerWidget` or `ConsumerStatefulWidget`.
- Descriptions for intentions to improve clarity when converting widgets.

### Changed

- Snippet group names have been renamed for clarity.
- Snippets have been updated and improved.

### Documentation

- Updated [snippets](snippets.md) to reflect the new structure and naming.
- Added screenshots

## [1.1.0](https://github.com/ginex25/RiverpodX/releases/tag/v1.1.0) - 2025-04-27

### Changed

- Updated `until-build` to `251.*` for compatibility with IntelliJ IDEA 2025.1.
    - Added support for recognizing providers with `@Riverpod` annotations, including support for GoToDefinition
      functionality.

### Notes

- **Navigation Issue**: After updating to this version, you may need to invalidate your IDE cache if you encounter
  issues with navigation. You can do this by selecting `File > Invalidate Caches... > Invalidate and Restart` in
  IntelliJ IDEA.

## [1.0.0](https://github.com/ginex25/RiverpodX/releases/tag/v1.0.0) - 2025-04-24

### Added

- GoTo Provider Declaration: Quickly navigate to the provider’s declaration.
    - Find Usages for Providers: Locate all usages of a provider in the project.
    - Code Snippets: Use predefined code snippets to save time.
    - Convert ConsumerWidget to ConsumerStatefulWidget: Easily switch between ConsumerWidget and ConsumerStatefulWidget.

### Info

- First release of RiverpodX, designed to boost Flutter development with useful Riverpod tools.
