import java.io.*;
import java.util.*;

// This program implements multiple pseudo-PageRank algorithms for ranking FGO classes.

public class FGOClassRanking
{
    // First declare the classes as constants for easy reference; probably should be using an enum,
    // but this seems to be shorter. The order is Shielder, Saber, Lancer, Archer, Caster, Assassin,
    // Rider, Ruler, Mooncancer, Avenger, Alterego, Foreigner, Pretender, Berserker.

    public static final int SHD=0, SAB=1, LAN=2, ARC=3, CAS=4, ASN=5, RID=6, RUL=7, MCR=8, AVG=9,
    ALR=10, FRN=11, PRT=12, BSK=13;

    public static final String[] classNames = {"SHD", "SAB", "LAN", "ARC", "CAS", "ASN", "RID",
    "RUL", "MCR", "AVG", "ALR", "FRN", "PRT", "BSK"};

    public static final int NUMCLS = 14;
    public static final double DAMPING = 0.1;
    public static final double MAXDMG = 2.0;

    // This array will hold the grid of class affinities.
    public static double[][] dmgTo;

    public static void main(String[] args)
    {
        makeGrid();

        //Print the grid for testing. Start with the top listing of names, and an indication of
        //attack vs defense.
        System.out.print("a/d ");
        for (int row = 0; row < NUMCLS; row++)
        {
            System.out.printf("%s ", classNames[row]);
        }
        System.out.println("");

        // Next iterate though the array to print its contents.
        for (int row = 0; row < NUMCLS; row++)
        {
            System.out.printf("%s", classNames[row]); //Start each row with the class name.
            for (int col = 0; col < NUMCLS; col++)
            {
                System.out.printf("%4.1f", dmgTo[row][col]);
            }
            System.out.println("");
        }

        double[] defRanks = defensivePageRank();
        printInOrder(defRanks, "Defensive rankings");
    }


    // I figured making this function was easier to do and check than typing it all out by hand.
    public static void makeGrid()
    {
        // This array will track the affinities; dmgTo[x][y] is how much damage x deals to y.
        dmgTo = new double[NUMCLS][NUMCLS];

        // Start with basic values of 1 for everything.
        for (int i = 0; i < NUMCLS; i++)
        {
            for (int j = 0; j < NUMCLS; j++)
            {
                dmgTo[i][j] = 1;
            }
        }

        // The next easiest thing to fill out is Berserker's bonuses (with some exceptions we'll
        // overwrite later).
        for (int i = 1; i < NUMCLS; i++) //Skipping the neutral Shielder.
        {
            dmgTo[i][BSK] = 2; //Berserker deals 1.5x damage and takes 2x from everyone.
            dmgTo[BSK][i] = 1.5;
        }

        // Next are the four main trangles.
        int[] triangleStarts = {SAB, CAS, RUL, ALR};
        for (int startClass : triangleStarts)
        {
            for (int index = 0; index < 3; index++)
            {
                int class1 = startClass + index;
                int class2 = startClass + (index + 1) % 3; //Next class in the triangle.

                // Each class deals 2x damage to the next in the triangle, and take 0.5x from it.
                dmgTo[class1][class2] = 2;
                dmgTo[class2][class1] = .5;
            }
        }

        // Next we add in the Ruler's resistance to the six main classes.

        for (int i = 0; i < 6; i++)
        {
            dmgTo[SAB + i][RUL] = 0.5;
        }

        // Next, we add the Alterego and Pretender's relations to the main six.
        int[] knights = {SAB, LAN, ARC};
        for (int knight: knights)
        {
            dmgTo[ALR][knight] = 0.5;
            dmgTo[PRT][knight] = 1.5;
        }

        int[] cavalries = {CAS, ASN, RID};
        for (int cavalry: cavalries)
        {
            dmgTo[ALR][cavalry] = 1.5;
            dmgTo[PRT][cavalry] = 0.5;
        }

        // Finally, add Foreigner's special relationships.
        dmgTo[FRN][FRN] = 2;
        dmgTo[BSK][FRN] = 0.5;
    }


    // This does a defensive PageRank algorithm to rank classes on defense.
    public static double[] defensivePageRank()
    {
        double[] classRanks = new double[NUMCLS];
        Arrays.fill(classRanks, 1.0/NUMCLS); //All classes start with the same rank, and sum to 1.

        // Next, we need to find the links for the algorithm; A links to B if A does 0.5x damage
        // to B. First make some ArrayLists to store the links.

        ArrayList<ArrayList<Integer>> pageLinks = new ArrayList<ArrayList<Integer>>();

        for (int i = 0; i < NUMCLS; i++)
        {
            pageLinks.add(new ArrayList<Integer>());
        }

        // Now iterate over the array to find all of the links.
        for (int atk = 0; atk < NUMCLS; atk++)
        {
            for (int def = 0; def < NUMCLS; def++)
            {
                if (dmgTo[atk][def] < 1 && atk != def) //Cannot link to self.
                {
                    pageLinks.get(atk).add(def);
                }
            }
        }

        // Now print the links array for testing.
        /*for (int atk = 0; atk < NUMCLS; atk++)
        {
            System.out.printf("%s: ", classNames[atk]);
            int numLinks = pageLinks.get(atk).size();
            for (int linker = 0; linker < numLinks; linker++)
            {
                System.out.printf("%s, ", classNames[pageLinks.get(atk).get(linker)]);
            }
            System.out.println("");
        }*/

        // Now we can start iterating to find the class ranks.
        Scanner scnr = new Scanner(System.in);
        for (int iteration = 0; iteration < 1000; iteration++) //Repeat until definitely stable.
        {
            double[] newRanks = new double[NUMCLS]; //Array to hold the new classes.

            for (int node = 0; node < NUMCLS; node++) //Node is the class whose value we're using.
            {
                double valueLeft = classRanks[node];
                double dampingLoss = DAMPING * valueLeft; //Start by spreading around the damping.
                for (int otherCls = 0; otherCls < NUMCLS; otherCls++)
                {
                    if (otherCls != node)
                    {
                        newRanks[otherCls] += dampingLoss / (NUMCLS - 1);
                    }
                }
                valueLeft -= dampingLoss;

                // Next, split the rest of its value among its links (if any).
                ArrayList<Integer> myLinks = pageLinks.get(node);
                int numLinks = myLinks.size();
                if (numLinks == 0) //If no links, just keep the rest, no div 0s!
                {
                    newRanks[node] += valueLeft;
                }
                else
                {
                    for (int i = 0; i < numLinks; i++)
                    {
                        newRanks[myLinks.get(i)] += valueLeft / numLinks;
                    }
                }
            }
            classRanks = newRanks; // Assign the new rankings.

            // Print the resuts of each iteration for clarity.
            /*for (int i = 0; i < NUMCLS; i++)
            {
                System.out.printf("%s%6.3f, ", classNames[i], classRanks[i]);
            }
            System.out.println("");*/
        }

        return classRanks;
    }


    // This function prints the results of a pageRank in descending order.
    public static void printInOrder(double[] rankings, String rankingDesc)
    {
        boolean[] listed = new boolean[NUMCLS]; //Track which ones we've already displayed.

        System.out.printf("%s:\n", rankingDesc);

        for (int place = 1; place <= NUMCLS; place++)
        {
            double maxVal = 0.0;
            int maxInd = -1;

            for (int check = 0; check < NUMCLS; check++)
            {
                if (rankings[check] > maxVal && !listed[check])
                {
                    maxVal = rankings[check];
                    maxInd = check;
                }
            }

            System.out.printf("Place #%d: %s%8.5f\n", place, classNames[maxInd], maxVal);
            listed[maxInd] = true;
        }
    }
}
