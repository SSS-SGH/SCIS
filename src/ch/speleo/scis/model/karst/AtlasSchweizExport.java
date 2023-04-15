package ch.speleo.scis.model.karst;

import java.io.*;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name = "ATLAS_SCHWEIZ_EXPORT")
@Getter @Setter
public class AtlasSchweizExport implements Serializable {

	private static final long serialVersionUID = -6956855273408985663L;

    @Id
    @Column(name = "INVENTORY_NR", nullable = true, unique = true, precision=8)
    private Integer inventoryNr;
    @Column(name = "NAME", length=100, nullable = false)
	private String name;
    @Column(name = "TYPE", length=10, nullable = true)
	private String type;
    @Column(name = "LENGTH_INFO", length=20, nullable = true)
	private String lengthInfo;
    @Column(name = "DEPTH_INFO", length=20, nullable = true)
	private String depthInfo;
    @Column(name = "COORD_EAST", nullable = true, precision=7)
    private Integer coordEast;
    @Column(name = "COORD_NORTH", nullable = true, precision=7)
    private Integer coordNorth;
    @Column(name = "COORD_ALTITUDE", nullable = true, precision=5)
    private Integer coordAltitude;
    @Column(name = "EXPORT_LAST_ID", length=20, nullable = true)
	private String exportLastId;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inventoryNr == null) ? 0 : inventoryNr.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtlasSchweizExport other = (AtlasSchweizExport) obj;
		if (inventoryNr == null) {
			if (other.inventoryNr != null)
				return false;
		} else if (!inventoryNr.equals(other.inventoryNr))
			return false;
		return true;
	}

}
