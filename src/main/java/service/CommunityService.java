
package service;

import domain.Friendship;
import domain.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides operations for analyzing communities in the social network.
 * A community is represented by a connected component of the friendship graph.
 */
public class CommunityService {

    private final UserService userService;
    private final FriendshipService friendshipService;

    /**
     * Creates a community service using the user and friendship services.
     *
     * @param userService the service used to access users
     * @param friendshipService the service used to access friendships
     */
    public CommunityService(
            UserService userService,
            FriendshipService friendshipService) {

        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    /**
     * Converts an Iterable into a sequential Stream.
     *
     * @param <T> the type of elements in the iterable
     * @param elements the iterable containing the elements
     * @return a sequential stream of the elements
     */
    private static <T> Stream<T> streamOf(Iterable<T> elements) {
        return StreamSupport.stream(elements.spliterator(), false);
    }

    /**
     * Builds an undirected graph representing the social network.
     * Each user is a vertex and each friendship is an edge.
     *
     * @return the adjacency map representing the social network
     */
    private Map<Long, Set<Long>> buildGraph() {

        List<Friendship> friendships =
                streamOf(friendshipService.getAllFriendships())
                        .collect(Collectors.toList());

        return streamOf(userService.getAllUsers())
                .map(User::getId)
                .collect(Collectors.toMap(
                        Function.identity(),
                        userId -> friendships.stream()
                                .filter(friendship ->
                                        friendship.getFirstUser().getId().equals(userId)
                                                || friendship.getSecondUser().getId().equals(userId)
                                )
                                .map(friendship ->
                                        friendship.getFirstUser().getId().equals(userId)
                                                ? friendship.getSecondUser().getId()
                                                : friendship.getFirstUser().getId()
                                )
                                .filter(friendId ->
                                        userService.findUserById(friendId).isPresent()
                                )
                                .collect(Collectors.toCollection(LinkedHashSet::new)),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    /**
     * Recursively visits all users connected to the current user.
     * Visited users are added to the connected component.
     *
     * @param currentId the identifier of the current user
     * @param graph the adjacency map representing the social network
     * @param visited the set of already visited user identifiers
     * @param component the set collecting the connected component
     */
    private void visitComponent(
            Long currentId,
            Map<Long, Set<Long>> graph,
            Set<Long> visited,
            Set<Long> component) {

        if (!visited.add(currentId)) {
            return;
        }

        component.add(currentId);

        graph.get(currentId).forEach(neighbour ->
                visitComponent(neighbour, graph, visited, component)
        );
    }

    /**
     * Finds the connected component containing a given user.
     *
     * @param startId the identifier of the starting user
     * @param graph the graph representing the social network
     * @return the identifiers belonging to the connected component
     */
    private Set<Long> findComponent(
            Long startId,
            Map<Long, Set<Long>> graph) {

        Set<Long> visited = new HashSet<>();
        Set<Long> component = new LinkedHashSet<>();

        visitComponent(startId, graph, visited, component);

        return component;
    }

    /**
     * Finds all distinct connected components of the graph.
     *
     * @param graph the adjacency map representing the social network
     * @return a list containing the distinct connected components
     */
    private List<Set<Long>> findAllComponents(
            Map<Long, Set<Long>> graph) {

        List<Set<Long>> candidateComponents = graph.keySet().stream()
                .map(userId -> findComponent(userId, graph))
                .collect(Collectors.toList());

        return candidateComponents.stream()
                .filter(component ->
                        candidateComponents.stream()
                                .noneMatch(other ->
                                        other != component
                                                && other.size() > component.size()
                                                && other.containsAll(component)
                                )
                )
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of communities in the social network.
     *
     * @return the number of connected components in the friendship graph
     */
    public int getNumberOfCommunities() {

        Map<Long, Set<Long>> graph = buildGraph();

        return findAllComponents(graph).size();
    }

    /**
     * Recursively computes the shortest-path distances from a user.
     * The frontier contains the users discovered at the current distance.
     *
     * @param frontier the users to explore at the current distance
     * @param distance the current shortest-path distance
     * @param component the identifiers belonging to the connected component
     * @param graph the adjacency map representing the social network
     * @param distances the map storing shortest-path distances for visited users
     */
    private void exploreDistances(
            Set<Long> frontier,
            int distance,
            Set<Long> component,
            Map<Long, Set<Long>> graph,
            Map<Long, Integer> distances) {

        if (frontier.isEmpty()) {
            return;
        }

        frontier.forEach(userId ->
                distances.put(userId, distance)
        );

        Set<Long> nextFrontier = frontier.stream()
                .flatMap(userId -> graph.get(userId).stream())
                .filter(component::contains)
                .filter(neighbour -> !distances.containsKey(neighbour))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        exploreDistances(
                nextFrontier,
                distance + 1,
                component,
                graph,
                distances
        );
    }

    /**
     * Calculates the diameter of a connected component.
     * The diameter is the maximum shortest-path distance between
     * any two users in the component.
     *
     * @param component the user identifiers in the connected component
     * @param graph the graph representing the social network
     * @return the diameter of the component
     */
    private int calculateDiameter(
            Set<Long> component,
            Map<Long, Set<Long>> graph) {

        return component.stream()
                .mapToInt(startId -> {

                    Map<Long, Integer> distances = new HashMap<>();

                    Set<Long> frontier = new LinkedHashSet<>();
                    frontier.add(startId);

                    exploreDistances(
                            frontier,
                            0,
                            component,
                            graph,
                            distances
                    );

                    return distances.values().stream()
                            .mapToInt(Integer::intValue)
                            .max()
                            .orElse(0);
                })
                .max()
                .orElse(0);
    }

    /**
     * Finds the most sociable community in the social network.
     * The most sociable community is the connected component
     * having the largest graph diameter.
     *
     * @return the users belonging to the most sociable community,
     *         or an empty list if there are no users
     */
    public List<User> getMostSociableCommunity() {

        Map<Long, Set<Long>> graph = buildGraph();

        List<Set<Long>> components = findAllComponents(graph);

        return components.stream()
                .max(Comparator.comparingInt(
                        component -> calculateDiameter(component, graph)
                ))
                .orElseGet(LinkedHashSet::new)
                .stream()
                .map(userService::findUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
