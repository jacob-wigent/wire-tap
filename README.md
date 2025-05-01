# 🔌WireTap

**WireTap** is a serial monitor application for UART communication, built with JavaFX. It provides an intuitive, responsive interface for reading and visualizing serial data from microcontrollers and embedded systems in real time. Unlike basic serial tools, WireTap aims to be an all-in-one serial toolkit that combines real-time logging, customizable parsing, and live data graphing.

> *This project is currently in active development.*

## Why WireTap?

Many existing serial tools lack flexibility, especially when it comes to saving logs, parsing custom formats, or visualizing multiple values at once. WireTap is being developed to bridge that gap — offering a powerful, lightweight, and customizable interface for makers, engineers, and students working with serial data.

## Current Features

- Auto-detect and list available serial (COM) ports
- Dropdown selection for common baud rates
- Connect/disconnect to ports with status updates
- Real-time serial text output with auto-scroll
- Basic JavaFX-based GUI with event-driven behavior

## Planned Features

- File logging (save serial data to `.txt` or `.csv`)
- Auto-generated filenames with timestamps
- Timestamped messages in the output panel
- Serial plotting (graph numerical data live)
  - Support for multiple data series with toggles
  - Auto-scaling or manual Y-axis settings
  - Save graph view as PNG
- Scroll/pause output with optional auto-scroll
- Serial input (send commands back to the device)
- Customizable text parsing (e.g., labeled fields or JSON)
- Disconnection detection and auto-reconnect
- Live statistics (average, min, max)
- Keyboard shortcuts for key functions
- Help dialog with tooltips and documentation

## Technologies & Dependencies

- **Java 17+** — Application runtime and base language  
- **JavaFX** — Modern GUI framework ([openjfx.io](https://openjfx.io))  
- **jSerialComm** — Cross-platform serial communication library ([Fazecast/jSerialComm](https://github.com/Fazecast/jSerialComm))  
- **Maven** — Dependency management and build automation

## License

This project is licensed under the [MIT License](LICENSE).  
