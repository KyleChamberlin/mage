/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.sets.shadowmoor;

import java.util.UUID;
import mage.constants.CardType;
import mage.constants.Rarity;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.keyword.PersistAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.target.TargetPermanent;

/**
 *
 * @author LevelX2
 */
public class WoodfallPrimus extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("noncreature permanent");
    static {
        filter.add(Predicates.not(new CardTypePredicate(CardType.CREATURE)));
    }

    public WoodfallPrimus(UUID ownerId) {
        super(ownerId, 135, "Woodfall Primus", Rarity.RARE, new CardType[]{CardType.CREATURE}, "{5}{G}{G}{G}");
        this.expansionSetCode = "SHM";
        this.subtype.add("Treefolk");
        this.subtype.add("Shaman");

        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Trample
        this.addAbility(TrampleAbility.getInstance());
        // When Woodfall Primus enters the battlefield, destroy target noncreature permanent.
        Ability ability = new EntersBattlefieldTriggeredAbility(new DestroyTargetEffect(), false);
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
        // Persist
        this.addAbility(new PersistAbility());
    }

    public WoodfallPrimus(final WoodfallPrimus card) {
        super(card);
    }

    @Override
    public WoodfallPrimus copy() {
        return new WoodfallPrimus(this);
    }
}
