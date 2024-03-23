import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button, Menu, Icon } from "semantic-ui-react";

import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import CloudUpload from '@material-ui/icons/CloudUpload';
import moment from "moment";
import AddBox from '@material-ui/icons/AddBox';
import ArrowUpward from '@material-ui/icons/ArrowUpward';
import Check from '@material-ui/icons/Check';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import Clear from '@material-ui/icons/Clear';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Edit from '@material-ui/icons/Edit';
import FilterList from '@material-ui/icons/FilterList';
import FirstPage from '@material-ui/icons/FirstPage';
import LastPage from '@material-ui/icons/LastPage';
import Remove from '@material-ui/icons/Remove';
import SaveAlt from '@material-ui/icons/SaveAlt';
import Search from '@material-ui/icons/Search';
import ViewColumn from '@material-ui/icons/ViewColumn';

const tableIcons = {
    Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
    Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
    Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
    Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
    DetailPanel: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
    Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
    Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
    Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
    FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
    LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
    NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
    PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
    ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
    Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
    SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
    ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
    ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />)
    };


const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));


const EAC = (props) => {
    const classes = useStyles();
    const [eac, setEac] = useState({});
    const [facilities, setFacilities] = useState([]);
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [eacPatientsView, setEacPatientsView] = useState([])



     useEffect(() => {
      Facilities();
      loadEac();
    }, []);
    const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setFacilities(response.data.currentOrganisationUnitId);
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const loadEac = () => {
    axios
      .get(`${baseUrl}dqr/eac-summary`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setEac(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const viewDetail =(headerTitle,patientDemoObj)=>{
    setPatientDetail(true)
    setGetHeaderInfo(headerTitle)
    const eacDemo =patientDemoObj
    axios
          .get(`${baseUrl}dqr/patient-eac?indicator=${eacDemo}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setEacPatientsView(response.data);
            //console.log(response.data[0])
          })
          .catch((error) => {
            console.log(error);
          });
    }
    const BackToList=()=> {
      setPatientDetail(false)
    }


    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>EAC</h3>
                    <div className="col-xl-12 col-lg-12">
                    {!showPatientDetail &&(<>
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th> Variance </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of eligible patients with documented EAC commencement date
                                </td>
                                <td>{eac[0]?.eacCommencedNumerator}</td>
                                <td>{eac[0]?.eacCommencedDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: eac[0]?.eacCommencedPerformance >= 95 ? 'green' : eac[0]?.eacCommencedPerformance >= 90 ? 'yellow' : 'red', color: eac[0]?.eacCommencedPerformance >= 95 ? 'white' : eac[0]?.eacCommencedPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{eac[0]?.eacCommencedPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of eligible patients with documented EAC commencement date", "eac0" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of patient with completed EAC session with VL sample collection date documented 
                                </td>
                                <td>{eac[0]?.eacComDateNumerator}</td>
                                <td>{eac[0]?.eacComDateDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: eac[0]?.eacComDatePerformance >= 95 ? 'green' : eac[0]?.eacComDatePerformance >= 90 ? 'yellow' : 'red', color: eac[0]?.eacComDatePerformance >= 95 ? 'white' : eac[0]?.eacComDatePerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{eac[0]?.eacComDatePerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of patient with completed EAC session with VL sample collection date documented", "eac1" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of patient who completed EAC with documented VL result for post EAC
                                </td>
                                <td>{eac[0]?.postEacNumerator}</td>
                                <td>{eac[0]?.postEacDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: eac[0]?.postEacPerformance >= 95 ? 'green' : eac[0]?.postEacPerformance >= 90 ? 'yellow' : 'red', color: eac[0]?.postEacPerformance >= 95 ? 'white' : eac[0]?.postEacPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{eac[0]?.postEacPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of patient who completed EAC with documented VL result for post EAC", "eac2" )}
                                    >View</Button>
                                </div>
                                </td>
                            
                            </tr>
                            

                            </tbody>
                        </Table>
                        </>)}
                        {showPatientDetail &&(<>
                      <Button
                        variant="contained"
                        style={{backgroundColor:"#014d88", }}
                        className=" float-right mr-1"
                        //startIcon={<FaUserPlus />}
                        onClick={BackToList}
                        >
                        <span style={{ textTransform: "capitalize", color:"#fff" }}> {"<<"} Back </span>
                        </Button>
                        <br/>
                        <br/> 
                        <MaterialTable
                            icons={tableIcons}
                            title={getHeaderInfo}
                            columns={[

                              {
                                title: "Hospital Number",
                                field: "hospitalNumber",
                              },
                              { title: "Sex ", field: "sex", filtering: false },
                              { title: "Date Of Birth", field: "dob", filtering: false },
                              { title: "Status", field: "status", filtering: false },

                            ]}
                            data={ eacPatientsView.map((row) => ({
                              //Id: manager.id,
                              hospitalNumber: row.hospitalNumber,
                              sex: row.sex,
                              dob: row.dateOfBirth,
                              status:row.status

                            }))}

                            options={{
                              headerStyle: {
                                backgroundColor: "#014d88",
                                color: "#fff",
                              },
                              searchFieldStyle: {
                                width : '200%',
                                margingLeft: '250px',
                              },
                              filtering: false,
                              exportButton: true,
                              searchFieldAlignment: 'left',
                              pageSizeOptions:[10,20,100],
                              pageSize:10,
                              debounceInterval: 400
                            }}
                        />
                        </>)}
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default EAC