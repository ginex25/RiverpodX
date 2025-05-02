# Snippets

## Generator syntax

| Shortcut                  | Description                                     |
|---------------------------|:------------------------------------------------|
| `riverpodPart`            | Creates a part statement                        |
| `generatorVariable`       | Create a variable using generator               |
| `generatorFutureVariable` | Create a future variable using generator        |
| `notifierProvider`        | Create a NotifierProvider using generator       |
| `asyncNotifierProvider`   | Create a AsyncNotifierProvider using generator  |
| `streamNotifierProvider`  | Create a StreamNotifierProvider using generator | 

> **Note:** All providers can be created with `keepAlive` and `family`.

## Common syntax

### Widgets

| Shortcut                 | Description                                                                                    |
|--------------------------|:-----------------------------------------------------------------------------------------------|
| `consumer`               | New Consumer                                                                                   |
| `consumerWidget`         | New ConsumerWidget                                                                             |
| `consumerStatefulWidget` | New ConsumerStatefulWidget                                                                     |
| `hookConsumer`           | New HookConsumer (must import [hooks_riverpod](https://pub.dev/packages/hooks_riverpod))       |
| `hookConsumerWidget`     | New HookConsumerWidget (must import [hooks_riverpod](https://pub.dev/packages/hooks_riverpod)) |
| `when`                   | Handle AsyncValue using when with data, error, and loading cases                               |

### Provider

| Shortcut                  | Description                                                                    |
|---------------------------|:-------------------------------------------------------------------------------|
| `changeNotifierProvider`* | New ChangeNotifierProvider                                                     |
| `provider*`               | New Provider                                                                   |
| `futureProvider`*         | New FutureProvider                                                             |
| `streamProvider`*         | New StreamProvider                                                             |
| `stateNotifier`           | New StateNotifier in [state_provider](https://pub.dev/packages/state_notifier) |
| `stateNotifierProvider`*  | New StateNotifierProvider                                                      |
| `stateProvider`*          | New StateProvider                                                              |

> **Note:** ( * )  All providers can be created with `keepAlive` and `family`.