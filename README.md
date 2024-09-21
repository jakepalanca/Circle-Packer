# Circle Packer

**Circle Packer** is a Java-based library that optimizes the placement of packable objects within a rectangular area. The primary use case is in data visualization, layout design, or any application requiring efficient packing algorithms. This library allows any Java object to be "packable" through a standardized interface, making it flexible and scalable.

While it includes a visual testing interface built with JavaFX, the main focus is on the back-end functionality of the packing algorithm, making it easy to integrate into your own projects.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
    - [Example Usage](#example-usage)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Flexible Packable Interface**: Any Java object can be made "packable" by implementing the provided `Packable` interface.
- **Efficient Optimization Algorithm**: Optimizes the layout of objects in a container, ensuring minimal overlaps and maximizing space usage.
- **Scalability**: Designed to handle different types of packable objects and scales for larger datasets.
- **Detailed Packing Results**: Outputs information such as computation time, overlap area, and adjustments made during the optimization process.

## Installation

You can easily integrate **Circle Packer** into your Java project using Maven.

### Maven Dependency

To use **Circle Packer** in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.jakepalanca</groupId>
    <artifactId>circle-packer</artifactId>
    <version>1.0.0</version>
</dependency>
```

Ensure that you refresh your Maven project or run the following command to pull the dependency:

```bash
mvn clean install
```

## Usage

After including the library, you can utilize the packing algorithm by creating and adding objects that implement the `Packable` interface.

### Example Usage

Here is a simple example showing how to create a chart, add objects, and optimize their placement using the **Circle Packer** library:

```java
import jakepalanca.circlepacker.Chart;
import jakepalanca.circlepacker.Packable;
import jakepalanca.circlepacker.PackingResult;
import javafx.scene.paint.Color;
import java.util.UUID;

public class CirclePackingExample {

    public static void main(String[] args) {
        // Create a chart with specified width and height
        Chart chart = new Chart(800, 600);

        // Create and add Packable objects (e.g., Bubbles)
        Packable bubble1 = new Bubble(1.0, Color.RED);
        Packable bubble2 = new Bubble(0.5, Color.BLUE);

        chart.addPackable(bubble1);
        chart.addPackable(bubble2);

        // Optimize the layout
        PackingResult<Packable> result = chart.optimize(1000);

        // Print out the results
        System.out.println("Computation Time: " + result.getComputationTime());
        System.out.println("Total Overlap Area: " + result.getTotalOverlapArea());
    }
}
```

This example demonstrates how to instantiate a `Chart`, add `Packable` objects (like `Bubble`), optimize the layout, and retrieve relevant results such as computation time and overlap area.

## Contributing

Contributions are welcome! If you find a bug or have an idea for an enhancement, feel free to submit a pull request or open an issue.

## License

This project is licensed under the GNU General Public License. See the [LICENSE](LICENSE.md) file for more details.
