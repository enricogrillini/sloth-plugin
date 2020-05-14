package sloth.maven.plugin;

public class BeanWriterTest {

//    @GenericTest
//    public void formScannerTest() throws JAXBException, FileNotFoundException {
//        File dbSchemaXml = getFileFromURL("db/itDistribuzioneIntranet.xml");
////
//        DbToolProject dbToolProject = new DbToolProject();
////        System.out.println( dbToolProject.toString());
////
////        JAXBContext jaxbContext     = JAXBContext.newInstance( DbToolProject.class );
////        Marshaller jaxbMarshaller   = jaxbContext.createMarshaller();
////
////        OutputStream os = new FileOutputStream( "d:/employee.xml" );
////        jaxbMarshaller.marshal( dbToolProject, os );
//
//        // Configuration
//        JAXBContext jaxbContext = JAXBContext.newInstance(DbToolProject.class);
//        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//         dbToolProject = (DbToolProject) jaxbUnmarshaller.unmarshal(dbSchemaXml);
//
//        for (Package dbPackage : dbToolProject.getDataBase().getPackages().getPackage()) {
//            PackageBeanWriter packageBeanWriter = new PackageBeanWriter(new File ("D:/"), "it.eg.sloth", dbPackage);
//            System.out.println(packageBeanWriter.getPackageBean());
//            break;
//        }
//    }
//
//    private static File getFileFromURL(String resource) {
//        URL url = BeanWriterTest.class.getClassLoader().getResource(resource);
//        File file = null;
//        try {
//            file = new File(url.toURI());
//        } catch (URISyntaxException e) {
//            file = new File(url.getPath());
//        }
//
//        return file;
//    }
}
