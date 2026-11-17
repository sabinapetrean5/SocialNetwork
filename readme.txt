
Advanced Programming Methods – 2026–2027

ToySocialNetwork is a Java application for managing users and friendships in a social network. It supports adding and removing users and friendships, calculating the number of connected communities, and identifying the most sociable community based on graph diameter.

The application follows a layered architecture that separates domain entities, validation, repositories, business logic, and console interaction. Generic repository interfaces allow different persistence implementations without changing the application's core functionality.

Three persistence mechanisms are supported: MEMORY (InMemoryRepository), FILE (UserFileRepository and FriendshipFileRepository), and DATABASE (UserDbRepository and FriendshipDbRepository). RepositoryFactory selects the implementation based on the repository.type property in config.properties. It uses a Map of Function objects instead of a switch statement to associate repository types with their creation logic.

The project uses Java, Gradle, and PostgreSQL with JDBC for database persistence.

Lab 4 introduced functional programming features, including lambda expressions, functional interfaces, method references, default interface methods, Optional, and Stream API. Traditional collection traversal loops were replaced with functional operations.

The following list summarizes the main refactorings (before and after):

Class:  ConsoleUI
Method: displayUsers()
Before: for-each loop
After:  StreamSupport.stream(), map(), collect(), forEach()

Class:  ConsoleUI
Method: displayFriendships()
Before: for-each loop
After:  StreamSupport.stream(), map(), collect(), forEach()

Class:  FriendshipService
Method: removeFriendshipsOfUser()
Before: for-each loop
After:  filter(), map(), collect(), forEach()

Class:  AbstractFileRepository
Method: loadData()
Before: Traditional loop
After:  Files.lines(), IntStream.range(), filter(), forEach()

Class:  AbstractFileRepository
Method: writeAllToFile()
Before: for-each loop
After:  stream(), map(), collect(), Files.write()

Class:  CommunityService
Method: buildGraph()
Before: Nested for-each loops
After:  map(), filter(), Collectors.toMap(), Collectors.toCollection()

Class:  CommunityService
Method: calculateDiameter()
Before: Iterative graph traversal
After:  Recursive exploration, flatMap(), filter(), mapToInt(), max()

Class:  CommunityService
Method: getMostSociableCommunity()
Before: for-each loops
After:  max(), map(), flatMap(), collect()

In FriendshipService.removeFriendshipsOfUser(), friendship IDs are collected before deletion to avoid modifying the repository during stream traversal. AbstractFileRepository.loadData() uses IntStream.range() to preserve line numbers when reporting corrupted records.

The Validator<T> interface is annotated with @FunctionalInterface and provides a default andThen() method for composing validation rules. In Main.java, the nameNoDigitsValidator lambda uses chars().anyMatch(Character::isDigit) and is combined with UserValidator.

The Repository<ID, E> interface defines a default findAll(Predicate<E> filter) method. UserCriteria provides reusable predicates for searching users by first or last name, while UserSearchDemo demonstrates Predicate.and(), or(), negate(), and Predicate.not().

Function is used in RepositoryFactory to create repository implementations, while Comparator.comparingInt() is used in CommunityService to compare community diameters. Method references such as System.out::println, Friendship::getId, InMemoryRepository::new, and LinkedHashSet::new simplify functional operations.

The Repository interface retains its original findOne(ID) contract, returning an entity or null when no matching entity exists. UserService.findUserById() wraps the result using Optional.ofNullable(), providing explicit handling of missing values without changing existing repository implementations. Optional operations such as orElseThrow(), orElse(), orElseGet(), and Optional::stream() are used instead of unchecked get() calls.

File persistence follows the Template Method design pattern. AbstractFileRepository contains common file-reading and writing logic, while UserFileRepository and FriendshipFileRepository implement entity-specific serialization and deserialization through extractEntity() and createEntityAsString().

Template Method was preferred over a generic file repository configured with Function<String, E> and Function<E, String> because the existing inheritance structure already separates shared operations from entity-specific behavior. This avoids duplication and preserves the established architecture.

File records use semicolon-separated values and ISO-formatted LocalDateTime values for friendships. Files are processed using Java NIO and UTF-8 encoding. Missing files are treated as empty repositories, while corrupted records are reported with their line numbers and skipped without interrupting the loading of valid data. I/O errors are wrapped in RepositoryException, preserving their original causes.

Lab 5 introduces PostgreSQL persistence through UserDbRepository and FriendshipDbRepository. Both implement Repository<ID, E> and provide save(), findOne(), findAll(), update(), and delete() operations.

The PostgreSQL database contains two tables:

- users: an automatically generated BIGINT primary key, first name, and last name.
- friendships: two user identifiers forming a composite primary key, together with the friendship creation timestamp.

User IDs are generated by PostgreSQL and retrieved by the application when new users are saved. The friendships table references users(id) through two foreign keys and enforces CHECK (id1 < id2) to maintain consistent friendship ordering and prevent self-friendships.

ON DELETE CASCADE was chosen instead of explicitly deleting friendships in the service layer. When a user is removed, PostgreSQL automatically deletes all friendships referencing that user. This maintains referential integrity at the database level, avoids additional deletion queries, and keeps the deletion logic independent of the service implementation.

Database repositories obtain connections through DriverManager using configuration parameters supplied by RepositoryFactory. SQL operations use PreparedStatement with parameterized queries, preventing user input from being interpreted as executable SQL. JDBC resources are managed using try-with-resources.

Repository operations validate entities before save() and update(). Null arguments produce IllegalArgumentException. The update() method returns the supplied entity when its ID does not exist, while delete() returns the deleted entity when found.

SQLExceptions are wrapped in RepositoryException while preserving their original causes. Repository failures, including connection errors and database constraint violations, are handled with meaningful messages rather than displaying stack traces in the console.

The social network is represented as an undirected graph, where users are vertices and friendships are edges. CommunityService identifies connected components to calculate the number of communities.

The most sociable community is defined as the connected component with the largest diameter, representing the maximum shortest-path distance between two vertices in that component. Graph processing uses recursive exploration and Stream API operations, including flatMap(), mapToInt(), max(), and Comparator.comparingInt().

The while (running) loop is preserved in ConsoleUI.run() because it controls interactive menu execution rather than traversing a collection. Collection processing uses functional operations instead of traditional traversal loops. Unnecessary anonymous classes and parallel streams are avoided.

New and modified classes and methods are documented using English Javadoc, including relevant @param, @return, and @throws annotations.