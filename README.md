# FGOClassRanking

So, I was inspired to create this program by some people who used the PageRank algorithm to give rankings to Pokemon classes ([here](https://www.reddit.com/r/pokemon/comments/8gef0t/i_used_a_pagerankstyle_algorithm_to_rank_pok%C3%A9mon/) and [here](https://towardsdatascience.com/recreational-data-science-whats-the-best-pok%C3%A9mon-type-d3fcd28ea740?gi=417ef27ef448)), as well as the recent reveal of a new class in FGO creating a [new affinity chart](https://www.reddit.com/r/grandorder/comments/oy1xux/i_made_a_new_affinity_diagram/). So I wanted to do something like that to rank classes in FGO.

Admittedly, I don't fully understand how PageRank works, plus it apparently doesn't handle weighted edges very well, so I wound up creating the best algorithm I could and hope it works.

Also, note on terminology: in addition to the "Knight triangle" for Saber/Lancer/Archer and the "Cavalry triangle" for Caster/Assassin/Rider, I refer to the Grail triangle for Ruler/Avenger/Mooncancer and the Outsider triangle for Foreigner/Alterego/Pretender, as well as the "main six" for the Knight and Cavalry triangles together.



## Algorithm Descriptions

Basically, my first two algorithms are similar to the offensive/defensive PageRank algorithms given in the first two links. For defensive PageRank, A links to B if A deals less damage to B. From there, for PageRank, each class starts with the same starting value. In each iteration of the algorithm, each class splits its value equally among all off the classes it links to and sends that part of its value there; however, a part of its value, called the "damping factor", is instead split among all the classes. This repeats until the values settle; classes can then be ranked by value. In the original algorithm, this is intended to simulate a web surfer randomly clicking links on pages, and occasionally going to an unrelated page. (Note that I used a damping factor of 10%, where 15% is apparently standard; should be easy to change the constant and rerun.)


For offensive PageRank, class A links to class B if B deals more damage against A (in PageRank, A linking to B is good for B); this seems to be the opposite of what the link says, but it makes sense to me right now. The rest of the algorithm is the same, with one wrinkle. Since there are 1.5x and 2.0x attack advantages, and PageRank doesn't support weighted edges by default, what do we do?

My first solution was to have 1.5x links instead transfer half of the value that would normally be given (since it's half the extra damage), and the rest stays where it is. This is referred to as "split" offensive rankings, since I split the two types of advantages. Thinking about it after the fact, this is not the right way to do it, since a 1.5x advantage will let you keep some of your value, unlike a 2.0x, making a 1.5x better than a 2.0x!

My second solution was just to ignore it and have all advantages be treated the same. I call these "unified" offensive rankings, since the 1.5x and 2.0x are treated the same.


I can get an overall value by averaging the results from the first two, but I also try to obtain this via my own Markov-style (AFAIK) algorithm. In this algorithm, every node is linked to every other with a weight for the damage affinity (0.5x, 1x, 1.5x, or 2x). Each node starts with the same value. Each step, the nodes split their current value evenly among each of the other nodes, but only give a part of that based on how well the other class can attack them, and keep the rest. If you want to be facetious ;-), you can imagine the classes all attacking each other to take their value, with the amount of value they are able to take dependent on how much damage they do.

For example, let's look at Saber and Archer; Saber deals 0.5x to Archer, but Archer deals 2x to Saber. In the algorithm, after both classes split their value into 14 portions for the 14 classes, they will give a part of this portion to each other. Since Archer deals the maximum possible damage to Saber, Saber will give all of its portion for Archer, but Archer will give Saber only 1/4 of its portion (since Saber can only deal 1/4th maximum damage to Archer) and keeps the rest. This again repeats until stabilization (hopefully).



## Results

The overall results for the rankings are as follows:


**Defensive rankings**
26.738% Ruler
24.644% Avenger
22.774% Mooncancer
 7.143% Shielder
 2.311% Saber
 2.311% Lancer
 2.311% Archer
 2.309% Alter Ego
 1.895% Caster
 1.895% Assassin
 1.895% Rider
 1.731% Foreigner
 1.279% Pretender
 0.763% Berserker

Now, having Ruler first and Berserker last in defensive rulings is reasonable, but the rest is pretty interesting. Mooncancer and Avenger being at the top right behind Ruler are surprising, especially since Avenger is meant to be an attacking class, but since this is purely based off the affinities, it makes sense that in a defensive meta full of Rulers, Avengers would find a place as a class that resists them, with Mooncancers hanging off their coattails (but pushed down by Ruler). Everything else I'm not sure of; Foreigner presumably bled the whole Outsider Triangle dry along with Berserker, but I'm not sure what happened with Alterego for them to get a boost above the Cavalries (and push the Knights above the Cavalries as well). This ends up being a common story, with the Grail and Outsider circles often getting dragged along with a specific member of them.


**Split offensive rankings**
14.522% Berserker
13.736% Alter Ego
12.892% Pretender
 7.601% Foreigner
 7.143% Shielder
 5.586% Ruler
 5.586% Mooncancer
 5.586% Avenger
 4.558% Saber
 4.558% Lancer
 4.558% Archer
 4.558% Caster
 4.558% Assassin
 4.558% Rider

In these rankings, the Outsider Circle is definitely the star of the show. With Berserker naturally rising to the top, Foreigner is able to feed off them to get a high rank as well, and the other two Outsiders are not only able to keep up but surpass Foreigner with their advantages over the Knight and Cavalry circles. This ends up pushing them below even the Grail Circle, who are best known for the tanky Grail class; they happen to be pretty well insulated from the Outsiders.


**Unified offensive rankings**
24.519% Berserker
12.820% Alter Ego
11.605% Pretender
 7.771% Foreigner
 7.143% Shielder
 4.677% Ruler
 4.677% Mooncancer
 4.677% Avenger
 3.685% Saber
 3.685% Lancer
 3.685% Archer
 3.685% Caster
 3.685% Assassin
 3.685% Rider

Turns out that my issue with the 1.5x affinities wasn't as big an issue as I thought, since the order here is the exact same as the split rankings. The only difference is that the gap is even bigger because, with Berserker's 1.5x attacks no longer counting against it, it's able to get to an even higher value than before.


**Overall rankings with/without split offensive**
15.935%/15.708% Ruler
14.888%/14.660% Avenger
13.953%/13.725% Mooncancer
10.142%/12.641% Berserker
 7.793%/ 7.565% Alter Ego
 7.143%/ 7.143% Shielder
 6.764%/ 6.442% Pretender
 4.708%/ 4.751% Foreigner
 3.216%/ 2.998% Saber
 3.216%/ 2.998% Lancer
 3.216%/ 2.998% Archer
 3.009%/ 2.790% Caster
 3.009%/ 2.790% Assassin
 3.009%/ 2.790% Rider

 I had two different ways of averaging out the results, since I was concerned about the split offensive issue: either I averaged the split and unified offensive for an overall offensive and then averaged that with defensive, or I just averaged unified offensive with defensive. I did that before seeing how similar the two offensive rankings were, and it ended up barely making a difference, so I just put them both in one section; the first number is with split offensive, the second is without.

 Anyways, looking at these results, it's interesting to see how the defensive king Ruler and offensive king Berserker went in the rankings; Ruler ends up with the upper hand since it was able to do decently in offensive, unlike Berserker who ended up dead last in defensive. The rest of the Grail team has a similar story, hanging off Ruler's coattails. Whatever advantage Alter Ego had in both ranking lets it muscle its way slightly above poor stuck-in-the-middle Shielder unlike the rest of its triangle, and the poor main six are left in the dust.
 

 **Markovian rankings**
  9.062% Ruler
  7.773% Foreigner
  7.379% Avenger
  7.292% Alter Ego
  7.243% Pretender
  7.205% Mooncancer
  7.143% Shielder
  6.945% Caster
  6.945% Assassin
  6.945% Rider
  6.945% Saber
  6.945% Lancer
  6.945% Archer
  5.232% Berserker

  Now, IDK how much I can trust my homemade ranking algorithm, but these results are definitely pretty odd. Ruler being at the top again works, since she has a bunch of resistances (who can't get as much value from her) without many weaknesses. Berserker at dead last may be shocking, but it makes some sense; Berserker essentially doesn't have a true advantage over anyone (it ties with Shielder and has a massive disadvantage against Foreigner, and with anyone else, he only deals 1.5x damage while taking 2.0x), so everyone else is able to bleed them dry.

  Also, the results are generally a lot closer than the other algorithms (1st place doesn't even have twice of last!), though that's likely because, unlike the other PageRank algorithms where the other classes give away all their value each iteration, this one has them keep a good deal of it, so last place doesn't drop as far, and first place can't get as much.

  The rest of it is a mystery, particularly how Foreigner is able to rise to the top when they can't get a boost from Berserker.
