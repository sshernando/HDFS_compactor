package utad.hdfs;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;


public class Compactador {
	
	static SequenceFile.Writer outseq=null;
	

	public static void main(String[] args) {
			
		try{
			//Carga de configuracion, instanciación FileSystem
			Configuration conf = new Configuration();
			FileSystem hdfs = FileSystem.get(new URI("hdfs://quickstart.cloudera:8020/"), conf);
			
			//Se comprueba si existe el fichero de destino, de ser asi se muestra mensaje y termina ejecución
			if (hdfs.exists(new Path(args[1]))){
				System.out.println("Error, la ruta de destino "+args[1]+" ya existe");
				System.exit(1);
			}
			
			//Se filtra el filesystem sobre el glob recibido como 1er parametro
			FileStatus fileStatus[] = hdfs.globStatus(new Path(args[0]));
			
			//Se comvierte a List por comodidad
			List<FileStatus> listFileStatus = Arrays.asList(fileStatus);
			
			//Se comprueba si hay algun fichero que coincida con el glob del parametro, de ser asi se instancia SequenceFile.writer
			if (!listFileStatus.isEmpty()){
				
				 outseq = SequenceFile.createWriter(
						conf,
						SequenceFile.Writer.file(hdfs.makeQualified(new Path(args[1]))),
						SequenceFile.Writer.keyClass(Text.class),
						SequenceFile.Writer.valueClass(Text.class)
				);
			}
			
			//Se incluyen todos los ficheros selecionados en el SequenceFile
			for (FileStatus fsAux: listFileStatus){
				
				Path fichero = fsAux.getPath();
				FSDataInputStream in = hdfs.open(fichero);
				
				byte[] content = new byte[(int)fsAux.getLen()];
				in.readFully(content);
				
				
				outseq.append(new Text(new String (fichero.toString())), new Text(new String(content)));	
				
			}
			
			outseq.close();
			hdfs.close();
				
			
		}catch (Exception e){
			System.out.println("Error de ejecución");
			e.printStackTrace();	
		}
		
		
	}//method
	
	
}//class
