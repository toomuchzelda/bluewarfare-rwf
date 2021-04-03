package me.libraryaddict.core.ranks;

import me.libraryaddict.core.rank.Rank;

import java.util.ArrayList;

public class PlayerRank {
    private ArrayList<RankInfo> _rankInfo = new ArrayList<RankInfo>();

    public PlayerRank(ArrayList<RankInfo> rankInfo) {
        _rankInfo = rankInfo;
    }

    public Rank getDisplayedRank() {
        Rank rank = Rank.ALL;

        for (RankInfo info : getInfo()) {
            if (info.hasExpired())
                continue;

            if (!info.getRank().isBetterRating(rank))
                continue;

            if (!info.isDisplay())
                continue;

            rank = info.getRank();
        }

        return rank;
    }

    public ArrayList<RankInfo> getInfo() {
        return _rankInfo;
    }

    public RankInfo getRank(Rank rank) {
        for (RankInfo info : getInfo()) {
            if (info.hasExpired())
                continue;

            if (info.getRank() != rank)
                continue;

            return info;
        }

        return null;
    }

    public boolean hasRank(Rank rank) {
        for (RankInfo info : getInfo()) {
            if (info.hasExpired())
                continue;

            if (info.getRank().ownsRank(rank))
                return true;
        }

        return Rank.ALL.ownsRank(rank);
    }

}
