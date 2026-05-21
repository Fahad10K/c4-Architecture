// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PizzaDelivery",
    platforms: [
        .iOS(.v17)
    ],
    dependencies: [
        .package(url: "https://github.com/Starscream/Starscream.git", from: "4.0.0"),
    ],
    targets: [
        .executableTarget(
            name: "PizzaDelivery",
            dependencies: ["Starscream"],
            path: "Sources"
        )
    ]
)
