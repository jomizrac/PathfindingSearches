import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class SearchUSA {
	public static ArrayList<City> cities = new ArrayList<City>();
	public static ArrayList<City> expanded = new ArrayList<City>();

	public static void main(String[] args) throws IOException {
		BuildGraph();
		City srcCity = findCity(args[1]);
		City destCity = findCity(args[2]);
		if (srcCity == null || destCity == null)
			System.err.println("No such City in the USA road!");
		Path solutionPath = new Path();
		if (args[0].equals("astar"))
			solutionPath = AstarSearch(srcCity, destCity);

		else if (args[0].equals("greedy"))
			solutionPath = greedySearch(srcCity, destCity);

		else if (args[0].equals("uniform"))
			solutionPath = uniformSearch(srcCity, destCity);
		else
			System.err.println("No such command!");
		showAllResults(solutionPath);
	}

	/**
	 * Method performs a AstarSearch
	 * 
	 * @param srcCity
	 *            the node to begin the search from
	 * @param destCity
	 *            the node being looked for
	 * @return the Path containing the solution found by the Astar algorithm
	 */
	private static Path AstarSearch(City srcCity, City destCity) {
		// create a solution path to be returned
		Path solutionPath = new Path();
		// int currentDist = 0;
		// srcCity.setHasExpanded(true);
		ArrayList<Path> myQueue = new ArrayList<Path>();
		// Path tempCities = new Path();
		City currentCity = srcCity;
		// currentCity.setHasExpanded(true);
		int numNow = 0;
		for (Map.Entry<City, Integer> temp : currentCity.getNeighbors()
				.entrySet()) {
			myQueue.add(new Path());
			myQueue.get(numNow).getPath().add(0, (City) srcCity);
			// myQueue.get(numNow).getPath().add(1, (City) temp.getKey());
			myQueue.get(numNow).setG(temp.getValue());
			myQueue.get(numNow).setH(CalcHeuristic(temp.getKey(), destCity));
			numNow++;
		}
		// tempCities.putAll(currentCity.getNeighbors());

		ArrayList<Path> possible = new ArrayList<Path>();
		boolean finished = false;
		boolean possibly = false;
		while (finished == false) {
			double chosenLength = 999999;
			if (myQueue.size() > 1) {
				int chosen = 0;
				chosenLength = 9999999;
				for (int i = 0; i < myQueue.size(); i++) {
					if ((myQueue.get(i).getG() + myQueue.get(i).getH()) <= (chosenLength)) {
						chosen = i;
						chosenLength = myQueue.get(i).getG()
								+ myQueue.get(i).getH();
					}
				}
				currentCity = myQueue.get(chosen).lastNode();

				solutionPath = myQueue.get(chosen);
				myQueue.remove(chosen);
				// System.out.println(currentCity.getName());

			} else if (myQueue.size() == 1) {
				solutionPath = myQueue.get(0);
				chosenLength = myQueue.get(0).getG() + myQueue.get(0).getH();
				myQueue.remove(0);
				// System.out.println(2);
			} else {
				solutionPath = null;
				finished = true;
				break;
			}
			if (currentCity.getName().equals(destCity.getName())) {
				possible.add(solutionPath);
				possibly = true;
			}
			// System.out.println(possibly);
			for (Map.Entry temp : solutionPath.lastNode().getNeighbors()
					.entrySet()) {
				City tempC = (City) temp.getKey();
				City tempA = findCity(tempC.getName());
				Path addedPath = new Path(solutionPath);
				if (tempA.isHasExpanded() == true
						&& solutionPath.getG() < tempC.getPastHighest()) {
					for (int a = 0; a < myQueue.size(); a++) {
						if (myQueue.get(a).lastNode().getName()
								.equals(tempA.getName())) {
							myQueue.remove(a);
						}
					}
					tempC.setPastHighest(addedPath.getG());
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength)
							.getPath()
							.add(myQueue.get(qLength).getPath().size(),
									(City) tempA);
					myQueue.get(qLength).setG(
							myQueue.get(qLength).getG()
									+ (Integer) temp.getValue());
					myQueue.get(qLength).setH(CalcHeuristic(tempA, destCity));
					// System.out.println("expanded");
				} else if (!tempA.isHasExpanded()) {
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength).getPath()
							.add(myQueue.get(qLength).getPath().size(), tempA);
					myQueue.get(qLength).setG(
							myQueue.get(qLength).getG()
									+ (Integer) temp.getValue());
					myQueue.get(qLength).setH(CalcHeuristic(tempA, destCity));
					currentCity.setHasExpanded(true);
					// System.out.println(addedPath.getPath().get(addedPath.getPath().size()-1).getName());
				}
			}
			if (possibly == true) {
				int amount = 0;
				for (int i = 0; i < myQueue.size(); i++) {
					if (possible.get(possible.size() - 1).getG() < (myQueue
							.get(i).getG() + myQueue.get(i).getH())) {
						amount++;
					}
					if (amount >= myQueue.size()) {
						finished = true;
					}
				}
				// System.out.println("chosen: " + chosenLength);
			}
			int shortest = 0;
			for (Path temp : possible) {
				if (temp.getG() < shortest) {
					solutionPath = temp;
				}
			}
		}
		return solutionPath;
	}

	/**
	 * Method performs a greedySearch
	 * 
	 * @param srcCity
	 *            the node to begin the search from
	 * @param destCity
	 *            the node being looked for
	 * @return the Path containing the solution found by the Greedy algorithm
	 */
	private static Path greedySearch(City srcCity, City destCity) {
		// create a solution path to be returned
		Path solutionPath = new Path();
		// int currentDist = 0;
		// srcCity.setHasExpanded(true);
		ArrayList<Path> myQueue = new ArrayList<Path>();
		// Path tempCities = new Path();
		City currentCity = srcCity;
		// currentCity.setHasExpanded(true);
		int numNow = 0;
		for (Map.Entry<City, Integer> temp : currentCity.getNeighbors()
				.entrySet()) {
			myQueue.add(new Path());
			myQueue.get(numNow).getPath().add(0, (City) srcCity);
			// myQueue.get(numNow).getPath().add(1, (City) temp.getKey());
			//myQueue.get(numNow).setG(temp.getValue());
			myQueue.get(numNow).setH(CalcHeuristic(temp.getKey(), destCity));
			numNow++;
		}
		// tempCities.putAll(currentCity.getNeighbors());

		ArrayList<Path> possible = new ArrayList<Path>();
		boolean finished = false;
		boolean possibly = false;
		while (finished == false) {
			double chosenLength = 999999;
			if (myQueue.size() > 1) {
				int chosen = 0;
				chosenLength = 9999999;
				for (int i = 0; i < myQueue.size(); i++) {
					if (myQueue.get(i).getH() <= (chosenLength)) {
						chosen = i;
						chosenLength = myQueue.get(i).getH();
					}
				}
				currentCity = myQueue.get(chosen).lastNode();

				solutionPath = myQueue.get(chosen);
				myQueue.remove(chosen);
				// System.out.println(currentCity.getName());

			} else if (myQueue.size() == 1) {
				solutionPath = myQueue.get(0);
				chosenLength = myQueue.get(0).getH();
				myQueue.remove(0);
				// System.out.println(2);
			} else {
				solutionPath = null;
				finished = true;
				break;
			}
			if (currentCity.getName().equals(destCity.getName())) {
				possible.add(solutionPath);
				possibly = true;
			}
			// System.out.println(possibly);
			for (Map.Entry temp : solutionPath.lastNode().getNeighbors()
					.entrySet()) {
				City tempC = (City) temp.getKey();
				City tempA = findCity(tempC.getName());
				Path addedPath = new Path(solutionPath);
				if (tempA.isHasExpanded() == true
						&& solutionPath.getG() < tempC.getPastHighest()) {
					for (int a = 0; a < myQueue.size(); a++) {
						if (myQueue.get(a).lastNode().getName()
								.equals(tempA.getName())) {
							myQueue.remove(a);
						}
					}
					tempC.setPastHighest(addedPath.getG());
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength)
							.getPath()
							.add(myQueue.get(qLength).getPath().size(),
									(City) tempA);
					myQueue.get(qLength).setH(CalcHeuristic(tempA, destCity));
					// System.out.println("expanded");
				} else if (!tempA.isHasExpanded()) {
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength).getPath()
							.add(myQueue.get(qLength).getPath().size(), tempA);
					myQueue.get(qLength).setH(CalcHeuristic(tempA, destCity));
					currentCity.setHasExpanded(true);
					// System.out.println(addedPath.getPath().get(addedPath.getPath().size()-1).getName());
				}
			}
			if (possibly == true) {
			//	int amount = 0;
			//	for (int i = 0; i < myQueue.size(); i++) {
			//		if (possible.get(possible.size() - 1).getG() < (myQueue.get(i).getH())) {
			//			amount++;
			//		}
			//		if (amount >= myQueue.size()) {
			//			finished = true;
			//		}
			//	}
				// System.out.println("chosen: " + chosenLength);
			finished = true;
			}
			
		}
		return solutionPath;
	}

	/**
	 * Method performs a uniformSearch
	 * 
	 * @param srcCity
	 *            the node to begin the search from
	 * @param destCity
	 *            the node being looked for
	 * @return the Path containing the solution found by the Uniform Cost
	 *         algorithm
	 */
	private static Path uniformSearch(City srcCity, City destCity) {
		// create a solution path to be returned
		Path solutionPath = new Path();
		// int currentDist = 0;
		// srcCity.setHasExpanded(true);
		ArrayList<Path> myQueue = new ArrayList<Path>();
		// Path tempCities = new Path();
		City currentCity = srcCity;
		// currentCity.setHasExpanded(true);
		int numNow = 0;
		for (Map.Entry<City, Integer> temp : currentCity.getNeighbors()
				.entrySet()) {
			myQueue.add(new Path());
			myQueue.get(numNow).getPath().add(0, (City) srcCity);
			// myQueue.get(numNow).getPath().add(1, (City) temp.getKey());
			myQueue.get(numNow).setG(temp.getValue());
			// myQueue.get(numNow).setH(CalcHeuristic(temp.getKey(), destCity));
			numNow++;
		}
		// tempCities.putAll(currentCity.getNeighbors());

		ArrayList<Path> possible = new ArrayList<Path>();
		boolean finished = false;
		boolean possibly = false;
		while (finished == false) {
			double chosenLength = 999999;
			if (myQueue.size() > 1) {
				int chosen = 0;
				chosenLength = 9999999;
				for (int i = 0; i < myQueue.size(); i++) {
					if ((myQueue.get(i).getG() + myQueue.get(i).getH()) <= (chosenLength)) {
						chosen = i;
						chosenLength = myQueue.get(i).getG()
								+ myQueue.get(i).getH();
					}
				}
				currentCity = myQueue.get(chosen).lastNode();

				solutionPath = myQueue.get(chosen);
				myQueue.remove(chosen);
				// System.out.println(currentCity.getName());

			} else if (myQueue.size() == 1) {
				solutionPath = myQueue.get(0);
				chosenLength = myQueue.get(0).getG() + myQueue.get(0).getH();
				myQueue.remove(0);
				// System.out.println(2);
			} else {
				solutionPath = null;
				finished = true;
				break;
			}
			if (currentCity.getName().equals(destCity.getName())) {
				possible.add(solutionPath);
				possibly = true;
			}
			// System.out.println(possibly);
			for (Map.Entry temp : solutionPath.lastNode().getNeighbors()
					.entrySet()) {
				City tempC = (City) temp.getKey();
				City tempA = findCity(tempC.getName());
				Path addedPath = new Path(solutionPath);
				if (tempA.isHasExpanded() == true
						&& solutionPath.getG() < tempC.getPastHighest()) {
					for (int a = 0; a < myQueue.size(); a++) {
						if (myQueue.get(a).lastNode().getName()
								.equals(tempA.getName())) {
							myQueue.remove(a);
						}
					}
					tempC.setPastHighest(addedPath.getG());
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength)
							.getPath()
							.add(myQueue.get(qLength).getPath().size(),
									(City) tempA);
					myQueue.get(qLength).setG(
							myQueue.get(qLength).getG()
									+ (Integer) temp.getValue());
					// myQueue.get(qLength).setH(CalcHeuristic(tempA,
					// destCity));

				} else if (!tempA.isHasExpanded()) {
					int qLength = myQueue.size();
					myQueue.add(addedPath);
					myQueue.get(qLength).getPath()
							.add(myQueue.get(qLength).getPath().size(), tempA);
					myQueue.get(qLength).setG(
							myQueue.get(qLength).getG()
									+ (Integer) temp.getValue());
					currentCity.setHasExpanded(true);
					// myQueue.get(qLength).setH(CalcHeuristic(tempA,
					// destCity));
					// System.out.println(addedPath.getPath().get(addedPath.getPath().size()-1).getName());
				}
			}
			if (possibly == true) {
				finished = true;
			}
		}

		return solutionPath;
	}

	private static void showAllResults(Path p) {
		System.out.print("Solution path: ");
		for (City c : p.getPath())
			System.out.print(c.getName() + " ");
		System.out.println();
		System.out.println("Path length: " + (p.getPath().size() - 1));
		System.out.println("Path cost: " + p.getG());
		System.out.print("Expanded nodes:");
		for (City c : expanded)
			System.out.print(c.getName() + " ");
		System.out.println();
		System.out.println("Number of expanded nodes: " + expanded.size());
	}

	/**
	 * 
	 * @param src
	 *            source city
	 * @param dest
	 *            destination city
	 * @return The heuristic cost from source to destination, computed using
	 *         decimal degrees of latitude and longitude.
	 */
	private static double CalcHeuristic(City src, City dest) {
		return Math
				.sqrt(Math.pow(69.5 * (src.latitude - dest.latitude), 2)
						+ Math.pow(
								69.5
										* (src.longitude - dest.longitude)
										* Math.cos(((src.latitude + dest.latitude) / 360)
												* Math.PI), 2));
	}

	/**
	 * 
	 * Builds the graph by creating all the cities and their neighbors and
	 * adding them to 'cities' ArrayList.
	 * 
	 */
	private static void BuildGraph() throws IOException {
		BufferedReader Cin = new BufferedReader(new FileReader("cities.txt"));
		BufferedReader Rin = new BufferedReader(new FileReader("roads.txt"));
		String str = null;
		cities = new ArrayList<City>();
		while (Cin.ready()) {
			str = Cin.readLine();
			str = str.replaceAll("\\s+", "");
			String[] param = str.split(",");
			cities.add(new City(param[0], Double.parseDouble(param[1]), Double
					.parseDouble(param[2])));
		}
		while (Rin.ready()) {
			str = Rin.readLine();
			str = str.replaceAll("\\s+", "");
			String[] params = str.split(",");
			City c1 = findCity(params[0]);
			City c2 = findCity(params[1]);
			c1.addNeighbor(c2, Integer.parseInt(params[2]));
			c2.addNeighbor(c1, Integer.parseInt(params[2]));
		}
	}

	/**
	 * 
	 * @param city
	 *            name of a city
	 * @return finds and returns the city with 'city' name in the graph.
	 */
	private static City findCity(String city) {
		if (cities == null)
			return null;
		for (City c : cities) {
			if (c.getName().equals(city))
				return c;
		}
		return null;
	}

}

class City {
	public String name;
	public boolean hasExpanded;
	public double longitude;
	public double latitude;
	public double pastHighest = 99999999;
	public HashMap<City, Integer> neighbors;

	public City(String name, double latitude, double longitude) {
		this.neighbors = new HashMap<City, Integer>();
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
		this.hasExpanded = false;
	}

	public boolean isHasExpanded() {
		return hasExpanded;
	}

	public void setHasExpanded(boolean hasExpanded) {
		this.hasExpanded = hasExpanded;
		SearchUSA.expanded.add(this);
	}

	public void addNeighbor(City n, Integer c) {
		neighbors.put(n, c);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPastHighest() {
		return pastHighest;
	}

	public void setPastHighest(double pastHighest) {
		this.pastHighest = pastHighest;
	}

	public HashMap<City, Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(HashMap<City, Integer> neighbors) {
		this.neighbors = neighbors;
	}
}

class Path {
	public LinkedList<City> path;
	public double g;
	public double h;

	public LinkedList<City> getPath() {
		return path;
	}

	public void setPath(LinkedList<City> path) {
		this.path = path;
	}

	public Path() {
		this.path = new LinkedList<City>();
		this.g = 0;
		this.h = 0;
	}

	public Path(Path another) {
		this.path = new LinkedList<City>();
		for (int i = 0; i < another.path.size(); i++) {
			this.path.add(i, another.path.get(i));
		}
		this.g = another.g;
		this.h = another.h;
	}

	public double getG() {
		return g;
	}

	public void setG(double g) {
		this.g = g;
	}

	public double getH() {
		return h;
	}

	public void setH(double d) {
		this.h = d;
	}

	public City lastNode() {
		if (path == null)
			return null;
		return path.getLast();
	}
}

