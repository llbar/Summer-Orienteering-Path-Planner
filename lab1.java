// Lachlan Bartle Lab1 Intro to AI
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
// Lab1 - A* search algorithm to find shortest path from point to point 
// based on terrain types and distance. Takes in inital terrain image, elevation file, path file, and output file to print the path to.
// Total distance and output image with shortest path are returned.
public class lab1
{
    // Width and height of each pixel (in m)
    static final double PIXEL_WIDTH = 10.29;
    static final double PIXEL_HEIGHT = 7.55;
    // Width and height of terrain image (in pixels)
    static final int IMAGE_WIDTH = 395;
    static final int IMAGE_HEIGHT = 500;
    // HashMap of terrain color values and their corresponding terrain types
    static final Map<Color, String> TERRAIN_COLORS = new HashMap<>();
    static 
    {
        TERRAIN_COLORS.put(new Color(71, 51, 3), "Paved road");
        TERRAIN_COLORS.put(new Color(248, 148, 18), "Open land");
        TERRAIN_COLORS.put(new Color(0, 0, 0), "Footpath");
        TERRAIN_COLORS.put(new Color(255, 255, 255), "Easy movement forest");
        TERRAIN_COLORS.put(new Color(2, 208, 60), "Slow run forest");
        TERRAIN_COLORS.put(new Color(2, 136, 40), "Walk forest");
        TERRAIN_COLORS.put(new Color(255, 192, 0), "Rough meadow");
        TERRAIN_COLORS.put(new Color(0, 0, 255), "Lake/Swamp/Marsh");
        TERRAIN_COLORS.put(new Color(5, 73, 24), "Impassible vegetation");
        TERRAIN_COLORS.put(new Color(205, 0, 101), "Out of bounds");
    }
    // HashMap of terrain types and their corresponding costs based on how hard it is
    // to travel through them.
    static final Map<String, Double> TERRAIN_COSTS = new HashMap<>();
    static 
    {
        TERRAIN_COSTS.put("Paved road", 1.0);
        TERRAIN_COSTS.put("Open land", 1.1);
        TERRAIN_COSTS.put("Footpath", 1.2);
        TERRAIN_COSTS.put("Easy movement forest", 1.6);
        TERRAIN_COSTS.put("Slow run forest", 2.1);
        TERRAIN_COSTS.put("Walk forest", 2.5);
        TERRAIN_COSTS.put("Rough meadow", 3.0);
        TERRAIN_COSTS.put("Lake/Swamp/Marsh", 1000.0);
        TERRAIN_COSTS.put("Impassible vegetation", Double.POSITIVE_INFINITY);
        TERRAIN_COSTS.put("Out of bounds", Double.POSITIVE_INFINITY);
    }
    // Pixel class for each pixel on image.
    static class Pixel 
    {
        // x and y coords for each pixel, terrain color of pixel, terrain type of pixel,
        // elevation of pixel, cost of pixel, parent of pixel.
        int x, y;
        Color terrainColor;
        String terrainType;
        double elevation;
        double cost;
        // Creates a pixel by taking in x and y coords and terrain color and elevation.
        Pixel(int x, int y, Color terrainColor, double elevation) 
        {
            this.x = x;
            this.y = y;
            this.terrainColor = terrainColor;
            this.terrainType = TERRAIN_COLORS.getOrDefault(terrainColor, "Out of bounds");
            this.elevation = elevation;
            this.cost = TERRAIN_COSTS.getOrDefault(terrainType, Double.POSITIVE_INFINITY);
        }
        // Returns cost of pixel.
        public double getCost() 
        {
            return cost;
        }
        // Sets cost of pixel.
        public void setCost(double cost) 
        {
            this.cost = cost;
        }
        // Calculates F score for pixel.
        public double getFScore(Pixel goal) 
        {
            // Uses the cost and adds it to the heuristic value from current pixel to goal pixel.
            double gScore = this.cost;
            double hScore = heuristic(this, goal);
            double fScore = gScore + hScore;
            return fScore;
        }
        // Equals functions for checking if pixel equals another pixel.
        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) 
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            Pixel other = (Pixel) obj;
            return x == other.x && y == other.y;
        }
        // Comparator for pixels ( i dont actually know if i will need this ).
        static class PixelComparator implements Comparator<Pixel> 
        {
            private Map<Pixel, Double> fScore;
            public PixelComparator(Map<Pixel, Double> fScore) 
            {
                    this.fScore = fScore;
            }
            @Override
            public int compare(Pixel pixel1, Pixel pixel2) 
            {
            return Double.compare(fScore.get(pixel1), fScore.get(pixel2));
            }
        }
        // Hash code.
        @Override
        public int hashCode() 
        {
            return Objects.hash(x, y);
        }
    }
    // Heuristic function for finding a path.
    public static double heuristic(Pixel pixel1, Pixel pixel2) 
    {
        // returns abs value of differences of x coords + abs value of differences of y values
        return Math.abs(pixel1.x - pixel2.x) + Math.abs(pixel1.y - pixel2.y) + Math.abs(pixel1.elevation-pixel2.elevation);
    }
    // Calculates the cost for each move.
    static double calculateCost(Pixel current, Pixel neighbor) 
    {
        // Finds abs value of diff of x and y values between current and neighboring pixel.
        double xDiff = Math.abs(current.x - neighbor.x); 
        double yDiff = Math.abs(current.y - neighbor.y); 
        double diffEle = Math.abs(current.elevation - neighbor.elevation);
        // Initalizes cost to 0.
        double cost = 0.0;
        // Checking if the movement is up/down or left/right.
        if (xDiff > yDiff) 
        {
            // When going left/right then the cost is the xDiff * PIXEL_WIDTH * cost of terrain.
            String terrainType = (neighbor.x > current.x) ? neighbor.terrainType : current.terrainType;
            double terrainCost = TERRAIN_COSTS.getOrDefault(terrainType, Double.POSITIVE_INFINITY);
            cost = xDiff * PIXEL_WIDTH * terrainCost + (0.5 * diffEle); 
        } else 
        {
            // When going up/down then the cost is the yDiff * PIXEL_HEIGht * cost of terrain.
            String terrainType = (neighbor.y > current.y) ? neighbor.terrainType : current.terrainType;
            double terrainCost = TERRAIN_COSTS.getOrDefault(terrainType, Double.POSITIVE_INFINITY);
            cost = yDiff * PIXEL_HEIGHT * terrainCost + (0.5 * diffEle);
        }
        return cost;
    }
    // Gets the pixel color at given pixel and returns it.
    static Color getPixelColor(BufferedImage terrainImage, int x, int y) 
    {
        int rgb = terrainImage.getRGB(x, y);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new Color(red, green, blue);
    }
    // Finds the neighbors for the current pixel.
    static List<Pixel> getNeighbors(Pixel current, BufferedImage terrainImage, Map<Integer, Double> elevationMapping) 
    {
        // Creating array list for neighbors.
        List<Pixel> neighbors = new ArrayList<>();
        // Grabbing current x and y values for current pixel.
        int x = current.x;
        int y = current.y;
        // Moves indicating going up, down, left, or right
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};   
        // For each move...
        for (int[] move : moves) 
        {
            // Grab the x and y of the move and calc current x + move x and current y + move y
            int dx = move[0];
            int dy = move[1];
            int newX = x + dx;
            int newY = y + dy;
            // Create neighbors as long as they are not out of bounds or impassible vegetation and add to list.
            // (make sure on image too)
            if (newX >= 0 && newX < IMAGE_WIDTH && newY >= 0 && newY < IMAGE_HEIGHT) 
            {
                Color neighborColor = getPixelColor(terrainImage, newX, newY);
                double neighborElevation = elevationMapping.getOrDefault(newX + newY * IMAGE_WIDTH, 0.0);
                String neighborTerrainType = TERRAIN_COLORS.getOrDefault(neighborColor, "Out of bounds");
                if (!neighborTerrainType.equals("Out of bounds") && !neighborTerrainType.equals("Impassible vegetation")) 
                {
                    neighbors.add(new Pixel(newX, newY, neighborColor, neighborElevation));
                }
            }
        }
        return neighbors;
    }
    // A* algorithm to find path
    static List<Pixel> astar(Pixel start, Pixel goal, BufferedImage terrainImage, Map<Integer, Double> elevationMapping) 
    {
        // Open set is a priority queue
        PriorityQueue<Pixel> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(goal), b.getFScore(goal)));
        // Puut the start pixel in the open set.
        openSet.offer(start);
        // Create new set of pixels for closed set.
        Set<Pixel> closedSet = new HashSet<>();
        // Create hashmap for pixel g scores.
        Map<Pixel, Double> gScore = new HashMap<>();
        // Put the start pixels g score in the map as 0.
        gScore.put(start, 0.0);
        // Create came from hash map for keeping track of pixels path.
        Map<Pixel, Pixel> cameFrom = new HashMap<>();
        // As long as the open set is not empty, find the path.
        while (!openSet.isEmpty()) 
        {
            // Current pixel is set to pixel at front of openset.
            Pixel current = openSet.poll();
            // If the current pixel is the goal pixel then construct the path.
            if (current.equals(goal)) 
            {
                return reconstructPath(cameFrom, current);
            }
            // Add the current pixel to the closed set.
            closedSet.add(current);
            // For each neighbor of current pixel.
            for (Pixel neighbor : getNeighbors(current, terrainImage, elevationMapping)) 
            {
                // If the closed set has the neighbor pixel in it then continue.
                if (closedSet.contains(neighbor)) 
                {
                    continue;
                }
                // Calculate tentative g score by adding current pixel g score to the calculated cost from current pixel to neighbor pixel.
                double tentativeGScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + calculateCost(current, neighbor);
                // If the neighbor pixel is in the open set or the tentative g sccore is less than the neighbor pixel's g score.
                if (!openSet.contains(neighbor) || tentativeGScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) 
                {
                    // Put the neighbor pixel, current pixel in the came from
                    cameFrom.put(neighbor, current); 
                    // Put the neighbor pixel's g score as the tentative g score in the g score map.
                    gScore.put(neighbor, tentativeGScore);
                    // Calculate f score as tentative g score + heuristic of the neighbor pixel to the goal pixel.
                    double fScore = tentativeGScore + heuristic(neighbor, goal);
                    // Set the cost of the neighbor pixel to the f score.
                    neighbor.setCost(fScore);
                    // Offer neighbor pixel to open set.
                    openSet.offer(neighbor);
                }
            }
        }
        // Returning empty list because if we got here there is no path.
        return Collections.emptyList();
    }
    // Reconstruct the path when we find the goal.
    static List<Pixel> reconstructPath(Map<Pixel, Pixel> cameFrom, Pixel current) 
    {
        // Create array list for path.
        List<Pixel> path = new ArrayList<>();
        // Add the current pixel to the path list.
        path.add(current);
        // While the came from map contains the current pixel as a key.
        while (cameFrom.containsKey(current)) 
        {
            // The current pixel equals the pixel attached to it in came from.
            current = cameFrom.get(current);
            // Add updated current to the list.
            path.add(current);
        }
        // Reverse the path for the correct order and return.
        Collections.reverse(path);
        return path;
    }
    // Main function takes in 4 args.
    public static void main(String[] args) 
    {
        // Checking if there are 4 args provided and if not returns error message and exits.
        if (args.length != 4) 
        {
            System.err.println("Usage: java Lab1 <terrain-image> <elevation-file> <path-file> <output-image-filename>");
            System.exit(1);
        }
        // Assigning all the args to correct corresponding string representations of paths.
        String terrainImagePath = args[0];
        String elevationFilePath = args[1];
        String pathFilePath = args[2];
        String outputImageFilename = args[3];
        // Using buffer image to read terrain image. If it cant it throws an error.
        BufferedImage terrainImage;
        try 
        {
            terrainImage = ImageIO.read(new File(terrainImagePath));
        } catch (IOException e) 
        {
            System.err.println("Error reading terrain image: " + e.getMessage());
            return;
        }
        // Create array list for the elevation value that correspond to each pixel.
        // Create a 2D array to store elevation values
        double[][] elevationMap = new double[IMAGE_HEIGHT][IMAGE_WIDTH];

        // Reads in each elevation value, if it can't, then it returns an error.
        try (BufferedReader elevationReader = new BufferedReader(new FileReader(elevationFilePath))) 
        {
            String line;
            int y = 0;
            while ((line = elevationReader.readLine()) != null && y < IMAGE_HEIGHT) 
            {
                String[] values = line.trim().split("\\s+");
                for (int x = 0; x < IMAGE_WIDTH && x < values.length; x++) 
                { 
                    elevationMap[y][x] = Double.parseDouble(values[x]);
                }
                y++;
            }
        } catch (IOException e) 
        {
            System.err.println("Error reading elevation data: " + e.getMessage());
            return;
        }

        // Create a hash map for mapping the elevations.
        Map<Integer, Double> elevationMapping = new HashMap<>();
        // Maps each elevation to the correct pixel coordinate.
        for (int y = 0; y < IMAGE_HEIGHT; y++) 
        {
            for (int x = 0; x < IMAGE_WIDTH; x++) 
            {
                double elevation = elevationMap[y][x];
                elevationMapping.put(x + y * IMAGE_WIDTH, elevation);
            }
        }
        // Create array list of pixels for the path points needed to be in path.
        List<Pixel> pathPoints = new ArrayList<>();
        // Reads in each point and creates a pixel for it and adds it to the list. If it cant it returns an error.
        try (BufferedReader pathReader = new BufferedReader(new FileReader(pathFilePath))) 
        {
            String line;
            while ((line = pathReader.readLine()) != null) 
            {
                String[] values = line.trim().split("\\s+");
                if (values.length == 2) 
                {
                    int x = Integer.parseInt(values[0]);
                    int y = Integer.parseInt(values[1]);
                    Color pixelColor = getPixelColor(terrainImage, x, y);
                    double elevation = elevationMapping.getOrDefault(x + y * IMAGE_WIDTH, 0.0);
                    pathPoints.add(new Pixel(x, y, pixelColor, elevation));
                }
            }
        } catch (IOException e) 
        {
            System.err.println("Error reading path data: " + e.getMessage());
            return;
        }
        // Using buffer image for the output image being the same as the terrain image.
        BufferedImage outputImage = new BufferedImage(terrainImage.getWidth(), terrainImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = outputImage.createGraphics();
        graphics.drawImage(terrainImage, 0, 0, null);
        // Initializing total distance to 0.0.
        double totalDistance = 0.0;
        // For each path point.
        for (int i = 0; i < pathPoints.size() - 1; i++) 
        {
            // Get the starting pixel and next pixel after that (the goal) and run the astar on them to get the path. Put path in a list of pixels.
            Pixel start = pathPoints.get(i);
            Pixel goal = pathPoints.get(i + 1);
            List<Pixel> path = astar(start, goal, terrainImage, elevationMapping);
            // If the path is empty then its done building.
            if (!path.isEmpty()) 
            {
                // For the path size.
                for (int j = 0; j < path.size() - 1; j++) 
                {
                    // Get the first pixel and the pixel after, draw the path between the two.
                    int x1 = path.get(j).x;
                    int y1 = path.get(j).y;
                    int x2 = path.get(j + 1).x;
                    int y2 = path.get(j + 1).y;
                    graphics.setColor(new Color(200, 100, 230));
                    graphics.drawLine(x1, y1, x2, y2);
                    // Fins the elevations of the two pixels.
                    double elevation1 = path.get(j).elevation;
                    double elevation2 = path.get(j + 1).elevation;
                    // The distance is determined by finding the sqrt of the change in x * PIXEL_WIDTH ^ 2 + change in y * PIXEL_HEIGHT ^ 2 + change in elevation ^ 2.
                    double distance = Math.sqrt(Math.pow((x2 - x1) * PIXEL_WIDTH, 2) + Math.pow((y2 - y1) * PIXEL_HEIGHT, 2) + Math.pow(elevation2 - elevation1, 2));
                    // Add the distance to the total distance.
                    totalDistance += distance;
                }
            }
            // Checking that a path was found.
            else
            {
                System.out.println("Path is empty.");}
        }
        // Printing out total distance and outputting the output image with the path drawn on. If it cant it returns error.
        try 
        {
            ImageIO.write(outputImage, "png", new File(outputImageFilename));
            System.out.println("Total Distance: " + totalDistance + " m");
        } catch (IOException e) 
        {
            System.err.println("Error writing output image: " + e.getMessage());
        }
    }
}
