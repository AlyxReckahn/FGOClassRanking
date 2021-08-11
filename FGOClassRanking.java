import java.io.*;
import java.util.*;

// This program implements multiple pseudo-PageRank algorithms for ranking FGO classes.

public class FGOClassRanking
{
    // First declare the classes as constants for easy reference; probably should be using an enum, but this seems to be shorter.
    // The order is Shielder, Saber, Lancer, Archer, Caster, Assassin, Rider, Ruler, Mooncancer,
    // Avenger, Alterego, Foreigner, Pretender, Berserker.

    public static final int SHD=0, SAB=1, LAN=2, ARC=3, CAS=4, ASN=5, RID=6, RUL=7, MCR=8, AVG=9,
    ALR=10, FRN=11, PRT=12, BSK=13;

    public static final String[] classNames = {"SHD", "SAB", "LAN", "ARC", "CAS", "ASN", "RID",
    "RUL", "MCR", "AVG", "ALR", "FRN", "PRT", "BSK"};

    /*public enum Serv {
        SHD (0),
        SAB (1),
        LAN (2),
        ARC (3),
        CAS (4),
        ASN (5),
        RID (6),
        RUL (7),
        MCR (8),
        AVG (9),
        ALR (10),
        FRN (11),
        PRT (12),
        BSK (13);

        private final int index;

        private Serv(int servInd)
        {
            this.index = servInd;
        }

        public int val()
        {
            return this.index;
        }
    }*/

    public static final int NUMCLS = 14;

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
        /*for (double[] row: dmgTo)
        {
            for (double entry: row)
            {
                System.out.printf("%4.1f", entry);
            }
            System.out.println("");
        }*/
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

        //finally, add Foreigner's special relationships.
        dmgTo[FRN][FRN] = 2;
        dmgTo[BSK][FRN] = 0.5;
    }
}
