---
sidebar_position: 1
---

# Basic Editing Examples

This page provides practical examples of common XML editing tasks using DomTrip. Each example shows both the input XML and the expected output.

## Maven POM Editing

### Adding a Dependency

```java
String pomXml = """
    <project xmlns="http://maven.apache.org/POM/4.0.0">
        <groupId>com.example</groupId>
        <artifactId>my-app</artifactId>
        <version>1.0.0</version>
        <dependencies>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.13.2</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </project>
    """;

Editor editor = new Editor(pomXml);

// Find dependencies section
Element dependencies = editor.findElement("dependencies");

// Add new dependency
Element newDep = editor.addElement(dependencies, "dependency");
editor.addElement(newDep, "groupId").setTextContent("org.mockito");
editor.addElement(newDep, "artifactId").setTextContent("mockito-core");
editor.addElement(newDep, "version").setTextContent("4.6.1");
editor.addElement(newDep, "scope").setTextContent("test");

String result = editor.toXml();
// Result preserves original formatting and adds new dependency with proper indentation
```

### Updating Version

```java
String pomXml = """
    <project>
        <groupId>com.example</groupId>
        <artifactId>my-app</artifactId>
        <version>1.0.0</version>
    </project>
    """;

Editor editor = new Editor(pomXml);

// Update version
Element version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");

String result = editor.toXml();
// Only the version element is modified, everything else preserved
```

## Configuration File Updates

### Spring Configuration

```java
String springXml = """
    <beans xmlns="http://www.springframework.org/schema/beans">
        <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
            <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
            <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
        </bean>
    </beans>
    """;

Editor editor = new Editor(springXml);

// Find the dataSource bean
Element dataSource = editor.findElement("bean[@id='dataSource']");

// Update database URL
Element urlProperty = dataSource.findChild("property[@name='url']");
editor.setAttribute(urlProperty, "value", "jdbc:mysql://prod-server:3306/mydb");

// Add new property
Element newProperty = editor.addElement(dataSource, "property");
editor.setAttribute(newProperty, "name", "maxActive");
editor.setAttribute(newProperty, "value", "100");

String result = editor.toXml();
```

### Web.xml Updates

```java
String webXml = """
    <web-app xmlns="http://java.sun.com/xml/ns/javaee" version="3.0">
        <servlet>
            <servlet-name>dispatcher</servlet-name>
            <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        </servlet>
    </web-app>
    """;

Editor editor = new Editor(webXml);

// Add servlet mapping
Element webApp = editor.getRootElement();
Element servletMapping = editor.addElement(webApp, "servlet-mapping");
editor.addElement(servletMapping, "servlet-name").setTextContent("dispatcher");
editor.addElement(servletMapping, "url-pattern").setTextContent("/api/*");

String result = editor.toXml();
```

## Data Processing

### Customer Data Update

```java
String customerXml = """
    <customers>
        <customer id="123">
            <name>John Doe</name>
            <email>john@example.com</email>
            <status>active</status>
        </customer>
        <customer id="456">
            <name>Jane Smith</name>
            <email>jane@example.com</email>
            <status>inactive</status>
        </customer>
    </customers>
    """;

Editor editor = new Editor(customerXml);

// Update specific customer
Element customer = editor.findElement("customer[@id='123']");
Element email = customer.findChild("email");
editor.setTextContent(email, "john.doe@newcompany.com");

// Add phone number
editor.addElement(customer, "phone").setTextContent("555-1234");

// Activate all inactive customers
editor.findElements("customer[status='inactive']")
    .forEach(c -> {
        Element status = c.findChild("status");
        editor.setTextContent(status, "active");
    });

String result = editor.toXml();
```

### Inventory Management

```java
String inventoryXml = """
    <inventory>
        <item sku="ABC123">
            <name>Widget A</name>
            <quantity>50</quantity>
            <price>19.99</price>
        </item>
    </inventory>
    """;

Editor editor = new Editor(inventoryXml);

// Update quantity
Element item = editor.findElement("item[@sku='ABC123']");
Element quantity = item.findChild("quantity");
int currentQty = Integer.parseInt(quantity.getTextContent());
editor.setTextContent(quantity, String.valueOf(currentQty - 10));

// Add new item
Element inventory = editor.getRootElement();
Element newItem = editor.addElement(inventory, "item");
editor.setAttribute(newItem, "sku", "XYZ789");
editor.addElement(newItem, "name").setTextContent("Widget B");
editor.addElement(newItem, "quantity").setTextContent("25");
editor.addElement(newItem, "price").setTextContent("29.99");

String result = editor.toXml();
```

## Template Processing

### Email Template

```java
String templateXml = """
    <email>
        <to>${recipient.email}</to>
        <subject>${email.subject}</subject>
        <body>
            <p>Dear ${recipient.name},</p>
            <p>${email.content}</p>
            <p>Best regards,<br/>${sender.name}</p>
        </body>
    </email>
    """;

Map<String, String> variables = Map.of(
    "recipient.email", "customer@example.com",
    "recipient.name", "John Doe",
    "email.subject", "Welcome to our service",
    "email.content", "Thank you for signing up!",
    "sender.name", "Customer Service Team"
);

Editor editor = new Editor(templateXml);

// Replace all placeholders
variables.forEach((placeholder, value) -> {
    String pattern = "${" + placeholder + "}";
    editor.findTextNodes()
        .filter(text -> text.getTextContent().contains(pattern))
        .forEach(text -> {
            String content = text.getTextContent().replace(pattern, value);
            editor.setTextContent(text, content);
        });
});

String result = editor.toXml();
```

## Namespace Handling

### SOAP Message Processing

```java
String soapXml = """
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Header>
            <auth:Authentication xmlns:auth="http://example.com/auth">
                <auth:username>user123</auth:username>
            </auth:Authentication>
        </soap:Header>
        <soap:Body>
            <order:CreateOrder xmlns:order="http://example.com/orders">
                <order:customerId>456</order:customerId>
            </order:CreateOrder>
        </soap:Body>
    </soap:Envelope>
    """;

Editor editor = new Editor(soapXml);

// Add authentication token
String authNS = "http://example.com/auth";
Element auth = editor.findElementByNamespace(authNS, "Authentication");
editor.addElementWithNamespace(auth, authNS, "token").setTextContent("abc123");

// Add order item
String orderNS = "http://example.com/orders";
Element createOrder = editor.findElementByNamespace(orderNS, "CreateOrder");
Element item = editor.addElementWithNamespace(createOrder, orderNS, "item");
editor.addElementWithNamespace(item, orderNS, "productId").setTextContent("PROD001");
editor.addElementWithNamespace(item, orderNS, "quantity").setTextContent("2");

String result = editor.toXml();
```

## Conditional Editing

### Feature Flag Configuration

```java
String configXml = """
    <configuration>
        <features>
            <feature name="newUI" enabled="false"/>
            <feature name="analytics" enabled="true"/>
        </features>
    </configuration>
    """;

Editor editor = new Editor(configXml);

// Enable/disable features based on conditions
Map<String, Boolean> featureFlags = Map.of(
    "newUI", true,
    "analytics", true,
    "betaFeatures", false
);

featureFlags.forEach((featureName, enabled) -> {
    Element feature = editor.findElement("feature[@name='" + featureName + "']");
    if (feature != null) {
        // Update existing feature
        editor.setAttribute(feature, "enabled", enabled.toString());
    } else {
        // Add new feature
        Element features = editor.findElement("features");
        Element newFeature = editor.addElement(features, "feature");
        editor.setAttribute(newFeature, "name", featureName);
        editor.setAttribute(newFeature, "enabled", enabled.toString());
    }
});

String result = editor.toXml();
```

## Bulk Operations

### Database Migration Script

```java
String migrationXml = """
    <migration>
        <table name="users">
            <column name="id" type="int" primary="true"/>
            <column name="username" type="varchar(50)"/>
            <column name="email" type="varchar(100)"/>
        </table>
    </migration>
    """;

Editor editor = new Editor(migrationXml);

// Add indexes for all varchar columns
Element table = editor.findElement("table[@name='users']");
editor.findElements("column[@type^='varchar']")
    .forEach(column -> {
        String columnName = column.getAttribute("name");
        Element index = editor.addElement(table, "index");
        editor.setAttribute(index, "name", "idx_" + columnName);
        editor.setAttribute(index, "column", columnName);
    });

// Add created_at timestamp to all tables
editor.findElements("table")
    .forEach(t -> {
        Element createdAt = editor.addElement(t, "column");
        editor.setAttribute(createdAt, "name", "created_at");
        editor.setAttribute(createdAt, "type", "timestamp");
        editor.setAttribute(createdAt, "default", "CURRENT_TIMESTAMP");
    });

String result = editor.toXml();
```

## Error Handling

### Safe Editing with Validation

```java
String xmlData = """
    <data>
        <item id="1">Value 1</item>
        <item id="2">Value 2</item>
    </data>
    """;

try {
    Editor editor = new Editor(xmlData);
    
    // Safe navigation with Optional
    editor.findElement("item[@id='3']")
        .ifPresentOrElse(
            item -> editor.setTextContent(item, "Updated Value"),
            () -> {
                // Item doesn't exist, create it
                Element data = editor.getRootElement();
                Element newItem = editor.addElement(data, "item");
                editor.setAttribute(newItem, "id", "3");
                editor.setTextContent(newItem, "New Value");
            }
        );
    
    String result = editor.toXml();
    
} catch (ParseException e) {
    System.err.println("Invalid XML: " + e.getMessage());
} catch (InvalidXmlException e) {
    System.err.println("Invalid operation: " + e.getMessage());
}
```

## Best Practices Summary

1. **Use targeted edits** - Modify only what needs to change
2. **Leverage Optional navigation** - Avoid null pointer exceptions
3. **Batch related changes** - Group modifications to the same element
4. **Preserve formatting** - Use appropriate DomTripConfig settings
5. **Handle namespaces properly** - Use namespace-aware methods when needed
6. **Validate inputs** - Check for null values and invalid operations

## Next Steps

- 🏗️ [Builder Patterns](../advanced/builder-patterns) - Creating complex structures
- ⚙️ [Configuration](../api/configuration) - Customizing DomTrip behavior
- 📚 [API Reference](../api/editor) - Complete Editor API documentation
