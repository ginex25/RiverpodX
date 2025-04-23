# RiverpodX

Boost your Flutter development with Riverpod! This plugin enhances your workflow by offering a set of useful tools
specifically designed for working with Riverpod.

## Features

- <b>GoTo Provider Declaration</b>: Quickly navigate to the provider’s declaration.
- <b>Find Usages for Providers</b>: Locate all usages of a provider in the project.
- <b>Code Snippets</b>: Use predefined code snippets to save time.
- <b>Convert `ConsumerWidget` to `ConsumerStatefulWidget` (and vice versa)</b>: Easily switch between ConsumerWidget and
  ConsumerStatefulWidget.

## Usage

### Snippets

#### Generator Syntax

| Shortcut                            | Description                                   |
|-------------------------------------|:----------------------------------------------|
| `riverpodPart`                      | Creates a part statement                      |
| `riverpodGeneratorVariable`         | Create a variable using the generator         |
| `riverpodGeneratorFutureVariable`   | Create a future variable using the generator  |
| `riverpodGeneratorNotifierProvider` | Create a NotifierProvider using the generator |

> **Note:** All providers can be created with `keepAlive` and `family`.

---

#### Common Syntax

| Shortcut                 | Description                                         |
|--------------------------|:----------------------------------------------------|
| `consumer`               | New Consumer                                        |
| `consumerWidget`         | New ConsumerWidget                                  |
| `consumerStatefulWidget` | New ConsumerStatefulWidget                          |
| `provider*`              | New Provider (suffix modifier: e.g., `autoDispose`) |
| `futureProvider*`        | New FutureProvider                                  |

> **Note:** ( * ) is suffix modifier, ex: autoDispose, family
---
For a full list of all available snippets, please check the [complete list of snippets](snippets.md).

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "RiverpodX"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/ginex25/RiverpodX/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Contributing

If you have suggestions or improvements for the plugin, feel free to contribute! Please follow these steps:

1. **Report Issues**: If you encounter bugs or have ideas for improvements, please open an issue on GitHub.
2. **Create a Branch**: Create a new branch for your feature or bug fix.
3. **Make Changes**: Implement your changes and ensure they align with the project's coding standards.
4. **Submit a Pull Request**: Once your changes are ready, submit a pull request with a clear description of what you've
   done.

Thank you for helping improve the plugin!

## License

This plugin is licensed under the Apache License Version 2.0. See the [LICENSE](LICENSE) file for more details.