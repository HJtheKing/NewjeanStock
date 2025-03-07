import React from "react";
import DiamondBadge from "./../assets/Diamond.png";
import PlatinumBadge from "./../assets/Platinum.png";
import GoldBadge from "./../assets/Gold.png";
import SilverBadge from "./../assets/Silver.png";
import BronzeBadge from "./../assets/Bronze.png";

const tierStyles = {
  Diamond: DiamondBadge,
  Platinum: PlatinumBadge,
  Gold: GoldBadge,
  Silver: SilverBadge,
  Bronze: BronzeBadge,
};

function TierBadge({ tier }) {
  return (
    <>
      {/* <span
        className={`px-2 py-1 rounded-full ${tierStyles[tier.slice(0, -2)]}`}
      >
        {tier.substr(-1)}
      </span> */}
      <img
        src={tierStyles[tier.slice(0, -2)]}
        alt="티어 뱃지"
        className="rounded-full"
      />
    </>
  );
}

export default TierBadge;
